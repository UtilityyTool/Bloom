//! The engine: holds the loaded plugins and hands each event only to the plugins
//! that are allowed to see it and haven't been paused for being slow.

use std::time::Duration;

use crate::budget::{Budget, Verdict};
use crate::caps::Grant;
use crate::manifest::{Permission, PluginManifest};

/// Something that happened in Discord that plugins can react to.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Event {
    /// A message arrived in a chat the user has open.
    MessageReceived,
    /// The user pressed send; a plugin may change the message before it goes out.
    BeforeSend,
    /// Discord's UI is ready; a plugin may add or change buttons.
    UiReady,
}

impl Event {
    /// The permission a plugin must hold to receive this event, if any.
    pub fn required(self) -> Option<Permission> {
        match self {
            Event::MessageReceived => Some(Permission::MessagesRead),
            Event::BeforeSend => Some(Permission::MessagesEditOutgoing),
            Event::UiReady => Some(Permission::UiChange),
        }
    }
}

/// How a plugin is currently doing.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Status {
    Active,
    /// Repeatedly over budget; still running, but flagged to the user.
    Slow,
    /// Turned off until restart (crashed, too slow, or a hook it needs is broken).
    Paused,
}

/// One loaded plugin and everything the runtime tracks about it.
#[derive(Debug, Clone)]
pub struct Plugin {
    pub manifest: PluginManifest,
    pub grant: Grant,
    pub budget: Budget,
    pub status: Status,
}

impl Plugin {
    pub fn new(manifest: PluginManifest) -> Self {
        let grant = Grant::from_manifest(&manifest);
        Plugin {
            manifest,
            grant,
            budget: Budget::default(),
            status: Status::Active,
        }
    }

    /// Should this plugin receive `event`? Only if it isn't paused and it holds
    /// the permission the event requires.
    pub fn wants(&self, event: Event) -> bool {
        self.status != Status::Paused
            && event.required().map_or(true, |p| self.grant.allows(p))
    }

    /// Turn a plugin off (e.g. a hook it needs broke after a Discord update).
    pub fn pause(&mut self) {
        self.status = Status::Paused;
    }
}

/// The set of plugins Bloom is running.
#[derive(Debug, Default)]
pub struct Runtime {
    pub plugins: Vec<Plugin>,
}

impl Runtime {
    pub fn new() -> Self {
        Runtime {
            plugins: Vec::new(),
        }
    }

    /// Load a plugin from its manifest.
    pub fn load(&mut self, manifest: PluginManifest) {
        self.plugins.push(Plugin::new(manifest));
    }

    /// How many plugins are currently on (active or slow, not paused).
    pub fn active_count(&self) -> usize {
        self.plugins
            .iter()
            .filter(|p| p.status != Status::Paused)
            .count()
    }

    /// Send an event to every plugin allowed to handle it. `run` stands in for the
    /// plugin's handler and returns how long it took; the runtime times it against the
    /// budget and pauses or flags the plugin as needed. Returns the ids that ran.
    pub fn dispatch<F>(&mut self, event: Event, mut run: F) -> Vec<String>
    where
        F: FnMut(&Plugin) -> Duration,
    {
        let mut ran = Vec::new();
        for plugin in self.plugins.iter_mut() {
            if !plugin.wants(event) {
                continue;
            }
            let elapsed = run(plugin);
            ran.push(plugin.manifest.id.clone());
            match plugin.budget.record(elapsed) {
                Verdict::Paused => plugin.status = Status::Paused,
                Verdict::Slow => {
                    if plugin.status == Status::Active {
                        plugin.status = Status::Slow;
                    }
                }
                Verdict::Warned | Verdict::Ok => {}
            }
        }
        ran
    }
}
