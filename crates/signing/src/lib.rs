//! Bloom plugin signing and verification.
//!
//! A **verified** plugin is one Bloom has signed with its private key. The app ships
//! Bloom's public key and checks the signature on install and update. If a plugin's
//! contents are tampered with, its digest changes and the signature stops matching —
//! so you can't edit a verified plugin and keep its badge.
//!
//! - Bloom's store (and the tooling/tests here) hold the **signing** key and sign plugins.
//! - The app holds only the **verifying** (public) key and checks signatures.

mod digest;

pub use digest::{digest_plugin, to_hex, PluginDigest};

use ed25519_dalek::{Signer, Verifier};
use rand_core::OsRng;

// Re-exported so callers don't need ed25519-dalek directly.
pub use ed25519_dalek::{Signature, SigningKey, VerifyingKey};

/// A signing key and its matching public key.
pub struct Keypair {
    pub signing: SigningKey,
    pub verifying: VerifyingKey,
}

/// Make a fresh Bloom signing key. Only Bloom's store side ever needs this.
pub fn generate_keypair() -> Keypair {
    let signing = SigningKey::generate(&mut OsRng);
    let verifying = signing.verifying_key();
    Keypair { signing, verifying }
}

/// Sign a plugin's digest with Bloom's private key.
pub fn sign(key: &SigningKey, digest: &PluginDigest) -> Signature {
    key.sign(digest.as_bytes())
}

/// Check a signature against Bloom's public key. True only if it's a real Bloom
/// signature over exactly this digest.
pub fn verify(key: &VerifyingKey, digest: &PluginDigest, signature: &Signature) -> bool {
    key.verify(digest.as_bytes(), signature).is_ok()
}

/// Where a plugin came from.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Source {
    /// Installed from the Bloom store.
    Store,
    /// Loaded from a folder on the device (Developer mode).
    LocalFolder,
}

/// How much a plugin is trusted — which decides the badge and the warnings.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Trust {
    /// Signed by Bloom, signature valid. Green check.
    Verified,
    /// From the store but not validly signed. Installable, with a warning.
    Community,
    /// Loaded locally for development. Warned every time it's enabled.
    Developer,
}

/// Decide a plugin's trust level. A store plugin is Verified only if it carries a
/// signature that checks out against Bloom's key; otherwise Community. Local plugins
/// are always Developer, signature or not.
pub fn trust_of(
    source: Source,
    digest: &PluginDigest,
    signature: Option<&Signature>,
    bloom_key: &VerifyingKey,
) -> Trust {
    match source {
        Source::LocalFolder => Trust::Developer,
        Source::Store => match signature {
            Some(sig) if verify(bloom_key, digest, sig) => Trust::Verified,
            _ => Trust::Community,
        },
    }
}
