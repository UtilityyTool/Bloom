//! Tests for the publish-time review: good plugins pass, bad ones are caught.

use bloom_review::{review, Severity, Verdict};
use bloom_runtime::PluginManifest;

fn manifest(permissions: &str, network: &str) -> PluginManifest {
    let json = format!(
        r#"{{"id":"p","name":"P","version":"1.0.0","description":"does a thing","permissions":[{permissions}],"network":[{network}]}}"#
    );
    PluginManifest::parse(&json).unwrap()
}

fn has_code(r: &bloom_review::Review, code: &str) -> bool {
    r.findings.iter().any(|f| f.code == code)
}

#[test]
fn the_real_example_plugin_passes() {
    let m = PluginManifest::parse(include_str!("../../../plugins/clean-links/plugin.json")).unwrap();
    let code = include_str!("../../../plugins/clean-links/index.js");
    let r = review(&m, code);
    assert_eq!(r.verdict, Verdict::Pass, "findings: {:?}", r.findings);
}

#[test]
fn using_a_permission_you_didnt_declare_fails() {
    let m = manifest("", ""); // declares nothing
    let code = "bloom.on('message', function(){ bloom.log(bloom.getMessageText()); });";
    let r = review(&m, code);
    assert_eq!(r.verdict, Verdict::Fail);
    assert!(has_code(&r, "undeclared-permission"));
}

#[test]
fn eval_fails() {
    let m = manifest("", "");
    let r = review(&m, "bloom.on('message', function(){ eval('1+1'); });");
    assert_eq!(r.verdict, Verdict::Fail);
    assert!(has_code(&r, "forbidden"));
}

#[test]
fn reading_messages_and_network_needs_a_human() {
    let m = manifest(r#""messages.read""#, r#""api.example.com""#);
    let code = "bloom.on('message', function(){ bloom.getMessageText(); });";
    let r = review(&m, code);
    assert_eq!(r.verdict, Verdict::NeedsHumanReview);
    assert!(has_code(&r, "reads-and-network"));
}

#[test]
fn a_wildcard_network_host_fails() {
    let m = manifest("", r#""*.example.com""#);
    let r = review(&m, "bloom.on('message', function(){});");
    assert_eq!(r.verdict, Verdict::Fail);
    assert!(has_code(&r, "network-wildcard"));
}

#[test]
fn over_declaring_a_permission_warns_but_passes() {
    let m = manifest(r#""storage""#, ""); // declares storage, never uses it
    let r = review(&m, "bloom.on('message', function(){ bloom.log('hi'); });");
    assert_eq!(r.verdict, Verdict::Pass);
    assert!(r.findings.iter().any(|f| f.code == "unused-permission" && f.severity == Severity::Warning));
}

#[test]
fn minified_code_fails() {
    let m = manifest("", "");
    let long_line = format!("bloom.on('message', function(){{ var x = '{}'; }});", "a".repeat(600));
    let r = review(&m, &long_line);
    assert_eq!(r.verdict, Verdict::Fail);
    assert!(has_code(&r, "obfuscation"));
}
