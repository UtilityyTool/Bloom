//! Automated publish-time review for Bloom plugins.
//!
//! Before a plugin can get the **Verified** badge, it has to pass these checks. They catch
//! the things a human reviewer would otherwise have to hunt for: code that uses a permission
//! it didn't declare, obfuscated code, forbidden escape hatches, and risky combinations that
//! deserve a human's eyes (like reading messages *and* sending data to a server).
//!
//! This is a heuristic static check over the manifest + code, not a full JS analysis. It's a
//! first gate: anything it can't be sure about, it escalates to a human rather than passing.

use bloom_runtime::{Permission, PluginManifest};

/// How serious a finding is.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Severity {
    /// Worth noting, doesn't block.
    Note,
    /// Should be fixed, doesn't block on its own.
    Warning,
    /// Blocks verification.
    Error,
}

/// One thing the review found.
#[derive(Debug, Clone)]
pub struct Finding {
    pub severity: Severity,
    pub code: &'static str,
    pub message: String,
}

/// The overall outcome.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Verdict {
    /// Safe to verify automatically.
    Pass,
    /// Nothing disqualifying, but something needs a human to look (e.g. reads + network).
    NeedsHumanReview,
    /// Can't be verified as-is.
    Fail,
}

/// The result of reviewing a plugin.
#[derive(Debug, Clone)]
pub struct Review {
    pub findings: Vec<Finding>,
    pub verdict: Verdict,
}

// JS API calls that require a permission. If the code uses one, the manifest must declare it.
const API_PERMS: &[(&str, Permission)] = &[
    ("bloom.getMessageText", Permission::MessagesRead),
    ("bloom.getOutgoingText", Permission::MessagesEditOutgoing),
    ("bloom.setOutgoingText", Permission::MessagesEditOutgoing),
    ("bloom.storage", Permission::Storage),
];

// Patterns that have no business in a plugin: escape hatches out of the sandbox, or
// attempts to reach things plugins must never touch.
const FORBIDDEN: &[(&str, &str)] = &[
    ("eval(", "uses eval()"),
    ("new Function", "builds code with new Function()"),
    ("globalThis", "reaches for globalThis"),
    ("document.cookie", "tries to read cookies"),
    ("localStorage", "tries to use localStorage"),
    ("XMLHttpRequest", "tries to make raw web requests"),
    ("import(", "uses dynamic import()"),
];

/// Review a plugin's manifest and code.
pub fn review(manifest: &PluginManifest, code: &str) -> Review {
    let mut f: Vec<Finding> = Vec::new();
    let mut has_error = false;
    let mut needs_human = false;

    // --- Manifest shape ---
    if !manifest.id.chars().all(|c| c.is_ascii_lowercase() || c.is_ascii_digit() || c == '-')
        || manifest.id.is_empty()
    {
        push(&mut f, Severity::Error, "id", "Plugin id must be lowercase letters, numbers and dashes.");
        has_error = true;
    }
    if manifest.name.len() > 30 {
        push(&mut f, Severity::Warning, "name", "Name is over 30 characters.");
    }
    if manifest.description.trim().is_empty() {
        push(&mut f, Severity::Warning, "description", "Add a description so users know what this does.");
    }

    // --- Network hosts: exact only ---
    for host in &manifest.network {
        if host.contains('*') {
            push(&mut f, Severity::Error, "network-wildcard", format!("Network host \"{host}\" uses a wildcard; list exact hosts only."));
            has_error = true;
        } else if !host.contains('.') {
            push(&mut f, Severity::Warning, "network-host", format!("Network host \"{host}\" doesn't look like a real hostname."));
        }
    }

    // --- Used-but-undeclared permissions ---
    for (pattern, perm) in API_PERMS {
        if code.contains(pattern) && !manifest.permissions.contains(perm) {
            push(&mut f, Severity::Error, "undeclared-permission",
                format!("Code calls {pattern}() but the manifest doesn't declare \"{}\".", perm.key()));
            has_error = true;
        }
    }

    // --- Declared-but-unused (only for the permissions we can detect) ---
    for perm in &manifest.permissions {
        let patterns: Vec<&str> = API_PERMS.iter().filter(|(_, p)| p == perm).map(|(s, _)| *s).collect();
        if !patterns.is_empty() && !patterns.iter().any(|s| code.contains(s)) {
            push(&mut f, Severity::Warning, "unused-permission",
                format!("Declares \"{}\" but doesn't seem to use it — only ask for what you need.", perm.key()));
        }
    }

    // --- Forbidden escape hatches ---
    for (pattern, desc) in FORBIDDEN {
        if code.contains(pattern) {
            push(&mut f, Severity::Error, "forbidden", format!("Plugin {desc}; that's not allowed."));
            has_error = true;
        }
    }

    // --- Obfuscation ---
    if code.lines().any(|l| l.len() > 500) {
        push(&mut f, Severity::Error, "obfuscation", "Has very long lines — looks minified or obfuscated. Submit readable source.");
        has_error = true;
    }

    // --- Risky-but-legitimate combos: escalate to a human ---
    let reads = manifest.permissions.contains(&Permission::MessagesRead) || code.contains("bloom.getMessageText");
    if reads && !manifest.network.is_empty() {
        push(&mut f, Severity::Note, "reads-and-network",
            "Reads messages and talks to the network — a human should confirm where it sends data.");
        needs_human = true;
    }

    let verdict = if has_error {
        Verdict::Fail
    } else if needs_human {
        Verdict::NeedsHumanReview
    } else {
        Verdict::Pass
    };

    Review { findings: f, verdict }
}

fn push(f: &mut Vec<Finding>, severity: Severity, code: &'static str, message: impl Into<String>) {
    f.push(Finding { severity, code, message: message.into() });
}
