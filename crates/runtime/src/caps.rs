//! What a plugin is actually allowed to do at runtime. Built from its manifest,
//! then checked on every action. A plugin can never do something it didn't declare.

use crate::manifest::{Permission, PluginManifest};

/// The set of things one plugin may do. Fields are private: the only way to act is
/// to ask, so a plugin can't quietly grant itself more.
#[derive(Debug, Clone)]
pub struct Grant {
    permissions: Vec<Permission>,
    hosts: Vec<String>,
}

impl Grant {
    /// Build the grant from what the manifest declared.
    pub fn from_manifest(m: &PluginManifest) -> Self {
        Grant {
            permissions: m.permissions.clone(),
            hosts: m.network.iter().map(|h| h.trim().to_lowercase()).collect(),
        }
    }

    /// Does this plugin hold the given permission?
    pub fn allows(&self, permission: Permission) -> bool {
        self.permissions.contains(&permission)
    }

    /// May this plugin talk to `host`? Exact match only — no wildcards, per the rulebook.
    pub fn allows_host(&self, host: &str) -> bool {
        let host = host.trim().to_lowercase();
        self.hosts.iter().any(|h| *h == host)
    }

    /// Every permission this plugin holds (for showing the user).
    pub fn permissions(&self) -> &[Permission] {
        &self.permissions
    }
}
