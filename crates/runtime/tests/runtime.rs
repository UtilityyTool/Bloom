//! Tests for the plugin runtime: the rulebook, enforced.

use bloom_runtime::*;
use std::time::Duration;

/// Build a manifest from parts, so each test reads clearly.
fn manifest(id: &str, permissions: &str, network: &str) -> PluginManifest {
    let json = format!(
        r#"{{"id":"{id}","name":"{id}","version":"1.0.0","permissions":[{permissions}],"network":[{network}]}}"#
    );
    PluginManifest::parse(&json).expect("manifest should parse")
}

#[test]
fn parses_a_manifest() {
    let m = manifest("clean-links", r#""messages.read","messages.edit-outgoing""#, "");
    assert_eq!(m.id, "clean-links");
    assert_eq!(m.permissions.len(), 2);
    assert!(m.permissions.contains(&Permission::MessagesRead));
    assert!(!m.uses_network());
    assert_eq!(m.top_risk(), Some(Risk::Medium));
}

#[test]
fn a_grant_denies_what_the_plugin_did_not_ask_for() {
    let g = Grant::from_manifest(&manifest("quiet", r#""ui.change""#, ""));
    assert!(g.allows(Permission::UiChange));
    assert!(!g.allows(Permission::MessagesRead)); // never declared, never allowed
}

#[test]
fn network_is_exact_host_only() {
    let g = Grant::from_manifest(&manifest(
        "translate",
        r#""messages.read""#,
        r#""translate.googleapis.com""#,
    ));
    assert!(g.allows_host("translate.googleapis.com"));
    assert!(g.allows_host("TRANSLATE.googleapis.com")); // case-insensitive
    assert!(!g.allows_host("evil.com"));
    assert!(!g.allows_host("sub.translate.googleapis.com")); // no wildcards
}

#[test]
fn network_use_counts_as_high_risk() {
    let m = manifest("translate", r#""messages.read""#, r#""translate.googleapis.com""#);
    assert!(m.uses_network());
    assert_eq!(m.top_risk(), Some(Risk::High));
}

#[test]
fn budget_warns_then_flags_slow_then_pauses() {
    let mut b = Budget::default();
    for _ in 0..4 {
        assert_eq!(b.record(Duration::from_millis(12)), Verdict::Warned);
    }
    assert_eq!(b.record(Duration::from_millis(12)), Verdict::Slow); // 5th time over budget
    assert_eq!(b.record(Duration::from_millis(60)), Verdict::Paused); // one very slow call
    assert_eq!(b.record(Duration::from_millis(1)), Verdict::Ok);
}

#[test]
fn an_event_only_reaches_plugins_with_the_permission() {
    let mut rt = Runtime::new();
    rt.load(manifest("reader", r#""messages.read""#, ""));
    rt.load(manifest("ui-only", r#""ui.change""#, ""));

    let ran = rt.dispatch(Event::MessageReceived, |_| Duration::from_millis(1));
    assert_eq!(ran, vec!["reader".to_string()]); // ui-only can't read messages
}

#[test]
fn a_slow_plugin_is_paused_and_then_skipped() {
    let mut rt = Runtime::new();
    rt.load(manifest("hog", r#""messages.read""#, ""));

    let first = rt.dispatch(Event::MessageReceived, |_| Duration::from_millis(60));
    assert_eq!(first, vec!["hog".to_string()]);
    assert_eq!(rt.plugins[0].status, Status::Paused);
    assert_eq!(rt.active_count(), 0);

    // A paused plugin no longer receives events.
    let second = rt.dispatch(Event::MessageReceived, |_| Duration::from_millis(1));
    assert!(second.is_empty());
}

#[test]
fn a_plugin_that_goes_slow_keeps_running_but_is_flagged() {
    let mut rt = Runtime::new();
    rt.load(manifest("chatty", r#""messages.read""#, ""));

    // Six events, each a little over one frame's budget.
    for _ in 0..6 {
        rt.dispatch(Event::MessageReceived, |_| Duration::from_millis(12));
    }
    assert_eq!(rt.plugins[0].status, Status::Slow); // flagged...
    assert_eq!(rt.active_count(), 1); // ...but still running
}
