# Host harness

"Bloom's brain" with the Discord connection stubbed. Given a plugin folder, it reads the manifest, works out the trust level, loads the plugin into the runtime with exactly its granted permissions, and runs it — everything the real host does except talk to Discord.

## Try it

```
cargo run -p bloom-host                       # runs the bundled clean-links example
cargo run -p bloom-host -- path/to/plugin     # any plugin folder
```

Example output:

```
Plugin:  Clean links 1.0.0  (by Bloom)
From:    local folder  ->  trust: Developer (local)
Digest:  eabe89ec8c1eed5c
Access:
   - Edits messages you send  [medium]

Sample: user sends "check this out youtube.com/watch?v=abc&si=track123"
  -> plugin changed it to "check this out youtube.com/watch?v=abc"
  ran in 2.1ms  (within budget)

Verified-badge check (using a throwaway Bloom key):
  store copy, signed by Bloom      -> Verified
  same copy after a tiny edit      -> Community (unverified)  (badge lost)
```

## What it proves

- **Load** a plugin from a folder (manifest + built JS).
- **Trust** it correctly: Developer (local), Community (store, unsigned), or Verified (store, signed by Bloom).
- **Enforce** permissions — the plugin only gets what its manifest declared.
- **Run** it against events and **time** it against the performance budget.

When the on-device loader into Discord is ready, it plugs straight into this — the harness is the piece that decides what runs and keeps it in line.

## Run the tests

```
cargo test -p bloom-host
```
