//! Tests for the JavaScript binding. Enforcement is checked from the plugin's side:
//! a plugin only sees and changes what its grant allows, even if its JS tries otherwise.
#![cfg(feature = "js")]

use bloom_runtime::js::{JsPlugin, Message};
use bloom_runtime::{Event, Grant, PluginManifest};

fn grant(permissions: &str) -> Grant {
    let json =
        format!(r#"{{"id":"p","name":"p","version":"1.0.0","permissions":[{permissions}]}}"#);
    Grant::from_manifest(&PluginManifest::parse(&json).expect("manifest parses"))
}

#[test]
fn runs_and_logs() {
    let mut p = JsPlugin::load(
        grant(""),
        "bloom.on('message', function (e) { bloom.log('hi'); });",
    )
    .unwrap();
    let mut m = Message { text: "x".into() };
    let o = p.dispatch(Event::MessageReceived, &mut m);
    assert!(o.ran);
    assert_eq!(o.logs, vec!["hi".to_string()]);
    assert!(o.error.is_none());
}

#[test]
fn read_allowed_with_permission() {
    let mut p = JsPlugin::load(
        grant(r#""messages.read""#),
        "bloom.on('message', function (e) { bloom.log(bloom.getMessageText()); });",
    )
    .unwrap();
    let mut m = Message { text: "hello".into() };
    let o = p.dispatch(Event::MessageReceived, &mut m);
    assert_eq!(o.logs, vec!["hello".to_string()]);
}

#[test]
fn read_blocked_without_permission() {
    let mut p = JsPlugin::load(
        grant(""),
        "bloom.on('message', function (e) { bloom.log(bloom.getMessageText()); });",
    )
    .unwrap();
    let mut m = Message { text: "secret".into() };
    let o = p.dispatch(Event::MessageReceived, &mut m);
    assert!(o.error.is_some());
    assert!(o.error.unwrap().contains("messages.read"));
    assert!(o.logs.is_empty()); // threw before it could log
}

#[test]
fn edit_outgoing_applies_when_permitted() {
    let mut p = JsPlugin::load(
        grant(r#""messages.edit-outgoing""#),
        "bloom.on('beforeSend', function (e) { bloom.setOutgoingText('clean'); });",
    )
    .unwrap();
    let mut m = Message {
        text: "dirty?si=1".into(),
    };
    p.dispatch(Event::BeforeSend, &mut m);
    assert_eq!(m.text, "clean");
}

#[test]
fn edit_outgoing_blocked_without_permission() {
    let mut p = JsPlugin::load(
        grant(""),
        "bloom.on('beforeSend', function (e) { bloom.setOutgoingText('clean'); });",
    )
    .unwrap();
    let mut m = Message {
        text: "original".into(),
    };
    let o = p.dispatch(Event::BeforeSend, &mut m);
    assert!(o.error.is_some());
    assert_eq!(m.text, "original"); // unchanged
}

#[test]
fn cannot_read_text_it_was_not_given() {
    // The real gate is Rust: with no read permission, the message text is never put into
    // the event, so even reading `e.text` directly (bypassing the bloom helper) sees nothing.
    let mut p = JsPlugin::load(
        grant(""),
        "bloom.on('message', function (e) { bloom.log(String(e.text)); });",
    )
    .unwrap();
    let mut m = Message {
        text: "topsecret".into(),
    };
    let o = p.dispatch(Event::MessageReceived, &mut m);
    assert_eq!(o.logs, vec!["undefined".to_string()]);
}

#[test]
fn example_clean_links_plugin_strips_trackers() {
    // Loads the real plugin from the repo: manifest + JS, through the whole runtime.
    let manifest_json = include_str!("../../../plugins/clean-links/plugin.json");
    let src = include_str!("../../../plugins/clean-links/index.js");
    let m = PluginManifest::parse(manifest_json).expect("example manifest parses");
    let mut p = JsPlugin::load(Grant::from_manifest(&m), src).expect("example plugin loads");

    let mut msg = Message {
        text: "look at this youtube.com/watch?v=abc&si=track123".into(),
    };
    let o = p.dispatch(Event::BeforeSend, &mut msg);
    assert!(o.error.is_none(), "unexpected error: {:?}", o.error);
    assert!(!msg.text.contains("si="), "tracker remained: {}", msg.text);
    assert!(msg.text.contains("v=abc"), "real param lost: {}", msg.text);
}
