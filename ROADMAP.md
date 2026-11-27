# Bloom roadmap

Where the project is and what's next. This is the running to-do list.

## Done

- **Brand** — Bud logo, Google Sans Flex Rounded wordmark, colours, app icon. Files in `assets/brand/`.
- **Bloom Manager (Android)** — the app that checks your Discord build and (later) installs/updates Bloom. Right now it's a **version checker**: it reads your installed Discord and says whether Bloom can run on it. Builds to an APK on every push (Releases → Test build).
- **Plugin rulebook** — `docs/plugin-rules.md`: permissions, risk levels, the never-allowed list, performance limits.
- **Plugin runtime (`crates/runtime`)** — the engine that runs plugins and enforces the rules:
  - manifest parsing, permission model, exact-host network rule
  - performance budget (warn → Slow → paused) so one plugin can't make Discord stutter
  - JavaScript binding (Boa) with the `bloom` API; permissions enforced at the Rust boundary
  - per-plugin storage (isolated, 5 MB cap)
  - 19 passing tests
- **Plugin author tooling** — typed API (`api/bloom.d.ts`) and a starter template (`plugins/_template`) that type-checks and builds.
- **Plugin signing (`crates/signing`)** — Ed25519 signatures over a plugin digest; tamper-proof Verified badge; trust levels. 5 tests.
- **Host harness (`crates/host`)** — ties it together: loads a plugin folder, decides its trust level, enforces permissions, runs it, times it. Runnable demo + 4 tests. This is Bloom's brain with the Discord connection stubbed.

## Next

- **TypeScript dev loop polish** — make Developer mode watch a plugin folder and hot-reload its built `dist/index.js`.
- **Plugin security: signing + verification** — the store-trust layer:
  - cryptographic signing (**built**: `crates/signing`, Ed25519 over a content digest, 5 tests — tampering breaks the badge, forged signatures rejected, trust levels resolve)
  - automated checks at publish time: code matches declared permissions, no obfuscation, flag risky combos (e.g. reads messages **and** sends to a server)
  - two tiers: **Verified** (reviewed + signed, green check) vs **Community** (installable with a clear "not reviewed" warning)
  - developers adding plugins locally are never flagged; flagging is a publish-time gate, and genuinely risky code is what it catches
  - **required warnings** (specced in the rulebook, mocked up): a warning every time Developer mode is turned on, and a warning every time an unverified plugin is enabled — both telling the user not to add plugins they were told to add or found outside the store
- **Plugin store** — browse/install from inside Discord's Bloom menu.

## Later

- **Vencord/BD porting** — not binary compatibility (Phoenix is native, their plugins target the old web Discord). Instead: a familiar API + a porting guide, and maybe a source shim for logic-only plugins. Honor licenses (Vencord is GPL-3.0, Bloom-compatible) and credit authors.
- **Host bridge** — connect the host harness to Discord's real UI and events (needs the on-device loader). The harness is ready for it.
- **Windows desktop** — installer, launcher, the drop-down menu, once Phoenix desktop reaches early access.
- **Remote sunset switch** — turn the Phoenix-unlock step off everywhere the day Discord ships Phoenix publicly.

## On hold (risky, on-device)

- **Phoenix unlock / loader** — getting Bloom running inside Discord by unlocking the staff-gated Phoenix build. This is the account-risk piece; it needs the real Discord binary and a device, so it's done hands-on against the real build, not from here. Path A (wait for Discord's official Phoenix release) remains the lower-risk alternative.

## Rules we hold to

- All code is original — nothing copied from other mods.
- Smooth or it doesn't ship — the runtime times every plugin.
- The user's account comes first — no touching the login token, no acting without the user.
