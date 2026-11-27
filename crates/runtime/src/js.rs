//! The JavaScript binding: loads a plugin's code into a JS engine, exposes the `bloom`
//! API to it, and dispatches events — with permissions enforced at the Rust boundary.
//!
//! The security rule: Rust decides what the plugin can see and what changes it can make,
//! based on the plugin's [`Grant`]. The JS-side `bloom` helpers also check, but they are a
//! convenience; the real gate is here, so a plugin can't get around it from JS.

use std::time::{Duration, Instant};

use boa_engine::{Context, Source};

use crate::caps::Grant;
use crate::engine::Event;
use crate::manifest::Permission;

/// A message a plugin may see or (for outgoing) change.
#[derive(Debug, Clone)]
pub struct Message {
    pub text: String,
}

/// What came out of handing one event to a plugin.
#[derive(Debug, Clone, Default)]
pub struct DispatchOutcome {
    /// A handler ran without throwing.
    pub ran: bool,
    /// Anything the plugin logged via `bloom.log(...)`.
    pub logs: Vec<String>,
    /// Set if the handler threw (e.g. it tried something it isn't allowed to).
    pub error: Option<String>,
    /// How long the handler took — fed to the performance budget.
    pub elapsed: Duration,
}

// The `bloom` API, defined in JS. The accessors check the injected `__grants`, but Rust
// only injects data the plugin may see and only reads back changes it may make.
const PRELUDE: &str = r#"
var __handlers = {};
var __log = [];
var __grants = [];
var __event = {};
var __storage = {};
function __has(p) { return __grants.indexOf(p) >= 0; }
var bloom = {
  on: function (ev, fn) { __handlers[ev] = fn; },
  log: function (m) { __log.push(String(m)); },
  getMessageText: function () {
    if (!__has('messages.read')) throw new Error('permission denied: messages.read');
    return __event.text;
  },
  getOutgoingText: function () {
    if (!__has('messages.edit-outgoing')) throw new Error('permission denied: messages.edit-outgoing');
    return __event.text;
  },
  setOutgoingText: function (t) {
    if (!__has('messages.edit-outgoing')) throw new Error('permission denied: messages.edit-outgoing');
    __event.outText = String(t);
  },
  storage: {
    get: function (k) {
      if (!__has('storage')) throw new Error('permission denied: storage');
      return __storage.hasOwnProperty(k) ? __storage[k] : null;
    },
    set: function (k, v) {
      if (!__has('storage')) throw new Error('permission denied: storage');
      __storage[String(k)] = String(v);
    },
    remove: function (k) {
      if (!__has('storage')) throw new Error('permission denied: storage');
      delete __storage[String(k)];
    },
    keys: function () {
      if (!__has('storage')) throw new Error('permission denied: storage');
      return Object.keys(__storage);
    }
  }
};
function __dispatch(ev) {
  var h = __handlers[ev];
  if (!h) return false;
  h(__event);
  return true;
}
"#;

/// Per-plugin storage cap. The rulebook allows a plugin up to 5 MB on the device.
pub const STORAGE_CAP: usize = 5 * 1024 * 1024;

/// A loaded JS plugin: its own JS engine context plus the grant that gates it.
pub struct JsPlugin {
    ctx: Context,
    grant: Grant,
    /// This plugin's saved storage, as a JSON object string. Isolated per plugin.
    stored: String,
    storage_cap: usize,
}

impl JsPlugin {
    /// Load a plugin from its granted permissions and its JS source.
    pub fn load(grant: Grant, source: &str) -> Result<Self, String> {
        let mut ctx = Context::default();
        eval(&mut ctx, PRELUDE)?;
        let keys: Vec<&str> = grant.permissions().iter().map(|p| p.key()).collect();
        let grants_json = serde_json::to_string(&keys).unwrap_or_else(|_| "[]".to_string());
        eval(&mut ctx, &format!("__grants = {};", grants_json))?;
        eval(&mut ctx, source).map_err(|e| format!("plugin failed to load: {e}"))?;
        Ok(JsPlugin {
            ctx,
            grant,
            stored: "{}".to_string(),
            storage_cap: STORAGE_CAP,
        })
    }

    /// Lower the storage cap (used in tests).
    pub fn with_storage_cap(mut self, cap: usize) -> Self {
        self.storage_cap = cap;
        self
    }

    /// This plugin's saved storage, as JSON — so the host can persist it to disk.
    pub fn storage_json(&self) -> &str {
        &self.stored
    }

    /// Restore saved storage (e.g. from disk on startup).
    pub fn load_storage(&mut self, json: &str) {
        self.stored = json.to_string();
    }

    /// Hand one event to the plugin. `msg` is the message in play; for `BeforeSend`, an
    /// allowed plugin may change its text. Returns what happened, for logging and the budget.
    pub fn dispatch(&mut self, event: Event, msg: &mut Message) -> DispatchOutcome {
        let kind = match event {
            Event::MessageReceived => "message",
            Event::BeforeSend => "beforeSend",
            Event::UiReady => "uiReady",
        };

        // Decide what the plugin may see this event.
        let can_see_text = match event {
            Event::MessageReceived => self.grant.allows(Permission::MessagesRead),
            Event::BeforeSend => {
                self.grant.allows(Permission::MessagesRead)
                    || self.grant.allows(Permission::MessagesEditOutgoing)
            }
            Event::UiReady => false,
        };
        let event_json = if can_see_text {
            format!("{{\"text\":{}}}", json_str(&msg.text))
        } else {
            "{}".to_string()
        };

        let mut outcome = DispatchOutcome::default();
        // Fresh event + log for this dispatch, plus this plugin's saved storage (only if allowed).
        let storage_init = if self.grant.allows(Permission::Storage) {
            self.stored.clone()
        } else {
            "{}".to_string()
        };
        let _ = eval(
            &mut self.ctx,
            &format!("__log = []; __event = {event_json}; __storage = {storage_init};"),
        );

        let start = Instant::now();
        let result = eval(&mut self.ctx, &format!("__dispatch('{kind}')"));
        outcome.elapsed = start.elapsed();

        match result {
            Ok(v) => outcome.ran = v == "true",
            Err(e) => outcome.error = Some(e),
        }

        // Collect logs.
        if let Ok(s) = eval(&mut self.ctx, "JSON.stringify(__log)") {
            if let Ok(parsed) = serde_json::from_str::<Vec<String>>(&s) {
                outcome.logs = parsed;
            }
        }

        // Persist storage changes, enforcing the cap. Rust is the gate: over cap, nothing saves.
        if self.grant.allows(Permission::Storage) {
            if let Ok(s) = eval(&mut self.ctx, "JSON.stringify(__storage)") {
                if s.len() <= self.storage_cap {
                    self.stored = s;
                } else {
                    outcome
                        .error
                        .get_or_insert_with(|| format!("storage limit exceeded ({} bytes)", self.storage_cap));
                }
            }
        }

        // Apply an outgoing edit only for BeforeSend and only if the plugin is allowed.
        if event == Event::BeforeSend && self.grant.allows(Permission::MessagesEditOutgoing) {
            if let Ok(s) = eval(
                &mut self.ctx,
                "(typeof __event.outText === 'string') ? __event.outText : null",
            ) {
                if s != "null" {
                    msg.text = s;
                }
            }
        }

        outcome
    }
}

// Evaluate JS and return the result as a plain string (numbers/bools become their text form).
fn eval(ctx: &mut Context, code: &str) -> Result<String, String> {
    match ctx.eval(Source::from_bytes(code)) {
        Ok(v) => {
            if let Some(s) = v.as_string() {
                Ok(s.to_std_string_escaped())
            } else {
                Ok(v.display().to_string())
            }
        }
        Err(e) => Err(e.to_string()),
    }
}

// JSON-encode a string (with surrounding quotes) so it's safe to inject into JS.
fn json_str(s: &str) -> String {
    serde_json::to_string(s).unwrap_or_else(|_| "\"\"".to_string())
}
