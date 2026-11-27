//! The Bloom host harness.
//!
//! This is "Bloom's brain" with the Discord connection stubbed: given a plugin folder,
//! it reads the manifest, works out the trust level (verified / community / developer),
//! loads the plugin into the runtime with exactly its granted permissions, and runs it
//! against sample events. Everything the real host does except talk to Discord.

use std::fs;
use std::path::Path;
use std::time::Duration;

use bloom_runtime::budget::{Budget, Verdict};
use bloom_runtime::js::{DispatchOutcome, JsPlugin, Message};
use bloom_runtime::{Event, Grant, PluginManifest};
use bloom_signing::{digest_plugin, trust_of, Signature, Source, Trust, VerifyingKey};

/// A plugin that's been read from disk, checked, and loaded into the runtime.
pub struct LoadedPlugin {
    pub manifest: PluginManifest,
    pub trust: Trust,
    pub digest_hex: String,
    pub plugin: JsPlugin,
    pub budget: Budget,
}

/// Read a plugin folder and load it.
///
/// `verify` is Bloom's public key plus the plugin's signature, when there is one. A
/// store plugin with a valid signature is Verified; without one, Community. A plugin
/// loaded from a local folder is always Developer.
pub fn load_plugin_dir(
    dir: &Path,
    source: Source,
    verify: Option<(&VerifyingKey, &Signature)>,
) -> Result<LoadedPlugin, String> {
    let manifest_json = read_file(dir, "plugin.json")?;
    let code = read_code(dir)?;
    let manifest =
        PluginManifest::parse(&manifest_json).map_err(|e| format!("bad plugin.json: {e}"))?;
    let digest = digest_plugin(&manifest_json, &code);

    let trust = match source {
        Source::LocalFolder => Trust::Developer,
        Source::Store => match verify {
            Some((key, sig)) => trust_of(Source::Store, &digest, Some(sig), key),
            None => Trust::Community,
        },
    };

    let grant = Grant::from_manifest(&manifest);
    let plugin = JsPlugin::load(grant, &code)?;
    Ok(LoadedPlugin {
        manifest,
        trust,
        digest_hex: digest.to_hex(),
        plugin,
        budget: Budget::default(),
    })
}

impl LoadedPlugin {
    /// Hand the plugin one event and record the timing against the budget.
    pub fn run(&mut self, event: Event, msg: &mut Message) -> (DispatchOutcome, Verdict) {
        let outcome = self.plugin.dispatch(event, msg);
        let verdict = self.budget.record(outcome.elapsed.max(Duration::from_micros(0)));
        (outcome, verdict)
    }
}

fn read_file(dir: &Path, name: &str) -> Result<String, String> {
    fs::read_to_string(dir.join(name)).map_err(|e| format!("can't read {name}: {e}"))
}

// Prefer a built bundle (dist/index.js), fall back to a plain index.js.
fn read_code(dir: &Path) -> Result<String, String> {
    if let Ok(s) = fs::read_to_string(dir.join("dist/index.js")) {
        return Ok(s);
    }
    read_file(dir, "index.js")
}
