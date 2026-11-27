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

## Run the tests

```
cargo test -p bloom-runtime
```

8 tests cover manifest parsing, permission denial, exact-host network rules, the performance budget, and event dispatch (slow plugins get paused and skipped).

## Not built yet

The JavaScript engine binding (so plugins can be written in TypeScript), persistent plugin storage, and the host bridge into Discord's real UI. Those come next.
