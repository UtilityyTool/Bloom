//! Bloom's plugin runtime.
//!
//! This is the part that runs plugins inside Discord and enforces the plugin rulebook
//! (docs/plugin-rules.md): every plugin declares its permissions, the runtime hands it
//! only the events it's allowed to see, and it times every handler so one slow plugin
//! can't make Discord stutter.
//!
//! It is deliberately engine-agnostic: the JavaScript engine that actually runs plugin
//! code plugs in on top of this. The rules live here so they're enforced no matter what.

pub mod budget;
pub mod caps;
pub mod engine;
pub mod manifest;

pub use budget::{Budget, Verdict};
pub use caps::Grant;
pub use engine::{Event, Plugin, Runtime, Status};
pub use manifest::{Permission, PluginManifest, Risk};
