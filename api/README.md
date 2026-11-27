# Plugin API

Types for plugin authors, so a plugin written against them actually runs on Bloom.

- **`bloom.d.ts`** — the `bloom` global: events, messages, and per-plugin storage. Reference it from your plugin with `/// <reference path="../../api/bloom.d.ts" />`.

Start from [`plugins/_template`](../plugins/_template), which already wires this up with type-checking and a build.
