//! A plugin's content digest: a SHA-256 fingerprint of its manifest + code.
//! Signing signs this, and verification re-computes it — so if a single byte of the
//! plugin changes, the digest changes and the signature no longer matches.

use sha2::{Digest, Sha256};

/// A SHA-256 fingerprint of a plugin's contents.
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PluginDigest([u8; 32]);

impl PluginDigest {
    /// The raw 32 bytes that get signed.
    pub fn as_bytes(&self) -> &[u8] {
        &self.0
    }

    /// Hex form, for storing or showing.
    pub fn to_hex(&self) -> String {
        to_hex(&self.0)
    }
}

/// Fingerprint a plugin from its `plugin.json` and its built JS. Deterministic, and
/// domain-separated with lengths so two different plugins can't collide by concatenation.
pub fn digest_plugin(manifest_json: &str, code: &str) -> PluginDigest {
    let mut h = Sha256::new();
    h.update(b"bloom-plugin-v1");
    h.update((manifest_json.len() as u64).to_le_bytes());
    h.update(manifest_json.as_bytes());
    h.update((code.len() as u64).to_le_bytes());
    h.update(code.as_bytes());
    let out = h.finalize();
    let mut d = [0u8; 32];
    d.copy_from_slice(&out);
    PluginDigest(d)
}

/// Lowercase hex, no external crate needed.
pub fn to_hex(bytes: &[u8]) -> String {
    let mut s = String::with_capacity(bytes.len() * 2);
    for b in bytes {
        s.push(char::from_digit((b >> 4) as u32, 16).unwrap());
        s.push(char::from_digit((b & 0xf) as u32, 16).unwrap());
    }
    s
}

/// Parse lowercase/uppercase hex back to bytes. Returns None if it isn't valid hex.
pub fn from_hex(s: &str) -> Option<Vec<u8>> {
    if s.len() % 2 != 0 {
        return None;
    }
    let b = s.as_bytes();
    let mut out = Vec::with_capacity(s.len() / 2);
    let mut i = 0;
    while i < b.len() {
        let hi = (b[i] as char).to_digit(16)?;
        let lo = (b[i + 1] as char).to_digit(16)?;
        out.push((hi * 16 + lo) as u8);
        i += 2;
    }
    Some(out)
}
