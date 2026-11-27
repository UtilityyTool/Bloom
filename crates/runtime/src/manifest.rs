//! A plugin's `plugin.json`: who it is and what it's allowed to touch.
//! This is the contract the rest of the runtime enforces.

use serde::Deserialize;

/// One thing a plugin can be allowed to do. The string names match `plugin.json`
/// and the plugin rulebook (docs/plugin-rules.md).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Deserialize)]
pub enum Permission {
    #[serde(rename = "ui.change")]
    UiChange,
    #[serde(rename = "storage")]
    Storage,
    #[serde(rename = "notifications")]
    Notifications,
    #[serde(rename = "clipboard.write")]
    ClipboardWrite,
    #[serde(rename = "messages.read")]
    MessagesRead,
    #[serde(rename = "messages.render")]
    MessagesRender,
    #[serde(rename = "messages.edit-outgoing")]
    MessagesEditOutgoing,
}

/// How risky a permission is. Shown to the user as a colour on the label.
#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub enum Risk {
    Low,
    Medium,
    High,
}

impl Permission {
    /// The risk level shown next to this permission.
    pub fn risk(self) -> Risk {
        match self {
            Permission::UiChange
            | Permission::Storage
            | Permission::Notifications
            | Permission::ClipboardWrite => Risk::Low,
            Permission::MessagesRead
            | Permission::MessagesRender
            | Permission::MessagesEditOutgoing => Risk::Medium,
        }
    }

    /// The label the user sees in the Bloom menu.
    pub fn label(self) -> &'static str {
        match self {
            Permission::UiChange => "Changes Discord's look",
            Permission::Storage => "Saves data on this device",
            Permission::Notifications => "Shows notifications",
            Permission::ClipboardWrite => "Copies to your clipboard",
            Permission::MessagesRead => "Reads messages",
            Permission::MessagesRender => "Changes how messages look to you",
            Permission::MessagesEditOutgoing => "Edits messages you send",
        }
    }
}

/// A plugin's declared identity and permissions, read from its `plugin.json`.
#[derive(Debug, Clone, Deserialize)]
pub struct PluginManifest {
    pub id: String,
    pub name: String,
    pub version: String,
    #[serde(default)]
    pub author: String,
    #[serde(default)]
    pub description: String,
    #[serde(default)]
    pub permissions: Vec<Permission>,
    /// Exact website names the plugin may talk to, e.g. `["translate.googleapis.com"]`.
    #[serde(default)]
    pub network: Vec<String>,
}

impl PluginManifest {
    /// Parse a `plugin.json`.
    pub fn parse(json: &str) -> Result<Self, serde_json::Error> {
        serde_json::from_str(json)
    }

    /// True if the plugin reaches the internet at all.
    pub fn uses_network(&self) -> bool {
        !self.network.is_empty()
    }

    /// The highest risk anything in this manifest carries (network counts as high).
    pub fn top_risk(&self) -> Option<Risk> {
        let perm = self.permissions.iter().map(|p| p.risk()).max();
        if self.uses_network() {
            Some(perm.map_or(Risk::High, |p| p.max(Risk::High)))
        } else {
            perm
        }
    }
}
