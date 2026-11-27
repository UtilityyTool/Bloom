/// <reference path="../../../api/bloom.d.ts" />

// A starter Bloom plugin. Edit this, then run `npm run build`.
// Add any permission you use (like "messages.read") to plugin.json first.

bloom.on("message", () => {
  bloom.log("Hello from my plugin!");
});
