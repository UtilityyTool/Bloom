//! Tests for the host harness: loading, trust, and running a real plugin folder.

use std::path::PathBuf;

use bloom_host::load_plugin_dir;
use bloom_runtime::js::Message;
use bloom_runtime::Event;
use bloom_signing::{digest_plugin, generate_keypair, sign, Source, Trust};

fn example_dir() -> PathBuf {
    PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("../../plugins/clean-links")
}

#[test]
fn loads_a_local_plugin_as_developer() {
    let loaded = load_plugin_dir(&example_dir(), Source::LocalFolder, None).unwrap();
    assert_eq!(loaded.manifest.id, "clean-links");
    assert_eq!(loaded.trust, Trust::Developer); // from a folder => Developer
    assert_eq!(loaded.digest_hex.len(), 64);
}

#[test]
fn runs_the_plugin_and_enforces_its_job() {
    let mut loaded = load_plugin_dir(&example_dir(), Source::LocalFolder, None).unwrap();
    let mut msg = Message {
        text: "youtube.com/watch?v=abc&si=track123".into(),
    };
    let (out, _verdict) = loaded.run(Event::BeforeSend, &mut msg);
    assert!(out.error.is_none());
    assert!(!msg.text.contains("si="), "tracker should be stripped: {}", msg.text);
    assert!(msg.text.contains("v=abc"));
}

#[test]
fn a_store_plugin_signed_by_bloom_is_verified() {
    let dir = example_dir();
    let manifest_json = std::fs::read_to_string(dir.join("plugin.json")).unwrap();
    let code = std::fs::read_to_string(dir.join("index.js")).unwrap();
    let digest = digest_plugin(&manifest_json, &code);

    let bloom = generate_keypair();
    let sig = sign(&bloom.signing, &digest);

    let loaded = load_plugin_dir(&dir, Source::Store, Some((&bloom.verifying, &sig))).unwrap();
    assert_eq!(loaded.trust, Trust::Verified);
}

#[test]
fn a_store_plugin_without_a_signature_is_community() {
    let loaded = load_plugin_dir(&example_dir(), Source::Store, None).unwrap();
    assert_eq!(loaded.trust, Trust::Community);
}
