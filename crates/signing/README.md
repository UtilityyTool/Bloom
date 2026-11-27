# Plugin signing (shared)

Makes the **Verified** badge real and tamper-proof.

A verified plugin is signed by Bloom (Ed25519) over a SHA-256 digest of its manifest + code. The app ships only Bloom's public key and checks the signature on install and update.

| File | What it does |
| --- | --- |
| `digest.rs` | SHA-256 fingerprint of a plugin's `plugin.json` + built JS. Change one byte, the fingerprint changes. |
| `lib.rs` | Sign, verify, and decide a plugin's trust level (**Verified** / **Community** / **Developer**). |

**The key property:** edit a verified plugin's code and its digest changes, so the old signature no longer matches — it silently drops from Verified to unverified. You can't tamper with a plugin and keep its badge.

- Bloom's store side holds the **signing** (private) key and signs plugins.
- The app holds only the **verifying** (public) key.

## Run the tests

```
cargo test -p bloom-signing
```

5 tests: sign/verify roundtrip, tampering breaks the signature, a forged signature from another key is rejected, trust levels resolve correctly, and the digest is stable and specific.

## Not built yet

The automated publish-time checks (code matches declared permissions, no obfuscation, risky-combo flags) and the store plumbing that calls this. The crypto core is here and tested.
