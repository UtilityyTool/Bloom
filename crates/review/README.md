# Publish-time review

The automated checks a plugin must pass before it can earn the **Verified** badge. It's the partner to signing: signing proves a plugin wasn't tampered with; this proves it was safe to verify in the first place.

It's a heuristic static check over the manifest + code — a first gate. Anything it can't be sure about, it escalates to a human rather than passing.

## What it catches

- **Used-but-undeclared permissions** — code that calls `bloom.getMessageText()` without declaring `messages.read` (fail).
- **Escape hatches** — `eval`, `new Function`, `localStorage`, `document.cookie`, `XMLHttpRequest`, dynamic `import()`, `globalThis` (fail).
- **Obfuscation** — minified / very-long-line code (fail; submit readable source).
- **Bad manifest** — wildcard network hosts, invalid id (fail); missing description, over-30-char name (warn).
- **Over-declaring** — asks for a permission it never uses (warn).
- **Risky-but-legitimate combos** — reads messages *and* uses the network; nothing disqualifying, but a human should confirm where it sends data (needs review).

## Try it

```
cargo run -p bloom-review                    # the clean-links example -> PASS
cargo run -p bloom-review -- path/to/plugin
```

A malicious "Free Nitro" plugin that reads messages, grabs a token from localStorage, and POSTs it to a server:

```
  ERROR    Code calls bloom.getMessageText() but the manifest doesn't declare "messages.read".
  ERROR    Plugin tries to use localStorage; that's not allowed.
  ERROR    Plugin tries to make raw web requests; that's not allowed.

Verdict: FAIL — can't be verified as-is.
```

## Run the tests

```
cargo test -p bloom-review
```

7 tests: the real example passes; undeclared permissions, eval, wildcard hosts, and minified code fail; over-declaring warns but passes; reads + network needs human review.
