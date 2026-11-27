//! Tests for plugin signing and verification — the "Verified" badge, made tamper-proof.

use bloom_signing::*;

const MANIFEST: &str = r#"{"id":"clean-links","name":"Clean links","version":"1.0.0","permissions":["messages.edit-outgoing"]}"#;
const CODE: &str = "bloom.on('beforeSend', function () {});";

#[test]
fn sign_and_verify_roundtrip() {
    let kp = generate_keypair();
    let d = digest_plugin(MANIFEST, CODE);
    let sig = sign(&kp.signing, &d);
    assert!(verify(&kp.verifying, &d, &sig));
}

#[test]
fn tampering_breaks_the_signature() {
    let kp = generate_keypair();
    let d = digest_plugin(MANIFEST, CODE);
    let sig = sign(&kp.signing, &d);

    // Someone edits the plugin's code after it was signed.
    let tampered = digest_plugin(MANIFEST, "bloom.on('beforeSend', function () { steal(); });");
    assert_ne!(d, tampered);
    assert!(!verify(&kp.verifying, &tampered, &sig)); // badge is lost
}

#[test]
fn a_signature_from_another_key_is_rejected() {
    let real = generate_keypair();
    let attacker = generate_keypair();
    let d = digest_plugin(MANIFEST, CODE);

    let forged = sign(&attacker.signing, &d);
    assert!(!verify(&real.verifying, &d, &forged)); // not Bloom's key, not valid
}

#[test]
fn trust_levels_are_decided_correctly() {
    let bloom = generate_keypair();
    let attacker = generate_keypair();
    let d = digest_plugin(MANIFEST, CODE);
    let good = sign(&bloom.signing, &d);
    let bad = sign(&attacker.signing, &d);

    // Store + a valid Bloom signature => Verified
    assert_eq!(
        trust_of(Source::Store, &d, Some(&good), &bloom.verifying),
        Trust::Verified
    );
    // Store + no signature => Community
    assert_eq!(
        trust_of(Source::Store, &d, None, &bloom.verifying),
        Trust::Community
    );
    // Store + a signature from the wrong key => Community (not Verified)
    assert_eq!(
        trust_of(Source::Store, &d, Some(&bad), &bloom.verifying),
        Trust::Community
    );
    // Loaded locally => Developer, regardless of signature
    assert_eq!(
        trust_of(Source::LocalFolder, &d, Some(&good), &bloom.verifying),
        Trust::Developer
    );
}

#[test]
fn digest_is_stable_and_specific() {
    // Same input, same digest.
    assert_eq!(digest_plugin(MANIFEST, CODE), digest_plugin(MANIFEST, CODE));
    // Different code, different digest.
    assert_ne!(digest_plugin(MANIFEST, CODE), digest_plugin(MANIFEST, "// other"));
    // Hex is 64 chars (32 bytes).
    assert_eq!(digest_plugin(MANIFEST, CODE).to_hex().len(), 64);
}
