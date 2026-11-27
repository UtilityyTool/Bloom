# Plugin runtime (shared)

The part that runs Bloom plugins inside Discord and enforces the [plugin rulebook](../../docs/plugin-rules.md).

It's engine-agnostic: the JavaScript engine that runs plugin code plugs in on top. The rules live here so they hold no matter what.

## What's built

| File | What it does |
| --- | --- |
| `manifest.rs` | Reads `plugin.json` — id, version, permissions, network hosts — and the risk level of each permission. |
| `caps.rs` | A plugin's **grant**: what it may actually do. A plugin can never do something it didn't declare. Network hosts are exact-match only (no wildcards). |
| `budget.rs` | The performance guard. ~8 ms is one frame on a 120 Hz phone; handlers over budget get warned, repeat offenders are flagged **Slow**, and one very slow call (50 ms+) pauses the plugin. |
| `engine.rs` | Holds the loaded plugins and sends each event only to plugins that hold the needed permission and aren't paused, timing every handler against the budget. |
| `js.rs` | The JavaScript engine binding (opt-in `js` feature): loads plugin JS, exposes the `bloom` API, and enforces permissions at the Rust boundary — a plugin only sees and changes what its grant allows. |

## Run the tests

```
cargo test -p bloom-runtime                # core rules
cargo test -p bloom-runtime --features js   # + the JavaScript binding
```

15 tests cover manifest parsing, permission denial, exact-host network rules, the performance budget, event dispatch (slow plugins get paused and skipped), and the JS binding — including loading the real `plugins/clean-links` plugin and checking it can only see and change what it's allowed to.

## Engine note

The JS binding uses Boa, a pure-Rust engine, so it builds and tests anywhere with no C toolchain. The runtime core is engine-agnostic, so a faster engine (e.g. QuickJS) can be swapped in later for performance without touching the rules.

## Not built yet

Persistent plugin storage, the TypeScript build step for plugin authors, and the host bridge into Discord's real UI. Those come next.
