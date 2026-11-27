<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/brand/bloom-lockup-light.svg">
    <img alt="Bloom" src="assets/brand/bloom-lockup-dark.svg" height="72">
  </picture>
</p>

# Bloom

Plugins and extra features for Discord's new native app (Phoenix), on Windows and Android.
Everything lives in one Material 3 menu, so Discord itself looks the same.

> **Early development.** Nothing here patches Discord yet.
> Client mods go against Discord's Terms of Service. Use at your own risk.
> Bloom is not made by or affiliated with Discord.

## What's where

| Folder | What it is |
| --- | --- |
| `android/manager/` | **Bloom Manager**, the Android app that installs and updates Bloom |
| `crates/installer/` | Windows setup wizard |
| `crates/launcher/` | Windows: starts Discord with Bloom loaded |
| `crates/loader-android/` | Android: the piece added into the Discord app |
| `crates/core/` | Runs inside Discord and starts everything else (shared) |
| `crates/hooks/` | Finds and hooks Discord's functions (shared) |
| `crates/runtime/` | Runs plugins and enforces their permissions (shared) |
| `crates/ui-desktop/` | Windows menu, settings and console |
| `crates/ui-android/` | Android bottom-sheet menu |
| `crates/shared/` | Settings, paths, logging |
| `plugins/` | Built-in plugins, one folder each, work on both platforms |
| `signatures/` | "Where to find things in Discord," per platform and Discord version |
| `api/` | Plugin API types for plugin authors |
| `assets/brand/` | Logo, wordmark and lockup files, plus how to use them |
| `tools/`, `docs/` | Our helper tools (including the logo generator) and guides |

## Try the Android test build

1. On your phone, open this repo's **Releases** page.
2. Under **Test build**, tap `bloom-manager.apk` to download it.
3. Open it and allow installing from your browser if Android asks.

The test build is the Bloom Manager app's look and setup flow only. It does not patch Discord yet.

## Ideas that shape the code

1. **Signatures are data, not code.** A Discord update that breaks a hook is fixed by shipping a new signature file.
2. **Plugins declare permissions in `plugin.json`, and the runtime enforces them.** The access labels users see are real limits.
3. **Nothing edits Discord's files on Windows.** The launcher loads Bloom when Discord starts.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) first. The short version: original code only, smooth or it doesn't ship, and the user's account comes first.

## License

Bloom is free software under the [GNU GPL v3](LICENSE). You can use, change and share it, but any version you share must stay open source under the same license.
