# Bloom plugin template

A starting point for a Bloom plugin. Copy this folder, rename it, and edit.

## Make it yours

1. Edit `plugin.json` — set the `id`, `name`, `description`, and any `permissions` you use.
2. Write your code in `src/index.ts`. You get autocomplete from the Bloom API types.
3. Build it:

   ```
   npm install
   npm run build        # makes dist/index.js
   npm run dev          # rebuilds on save, for development
   npm run typecheck    # catches mistakes before you ship
   ```

## Load it while developing

Turn on **Developer mode** in Bloom's settings and point it at this folder. Bloom loads `dist/index.js` and reloads it when it changes. Dev plugins run in the same sandbox as any other plugin — they can only do what their permissions allow — so you can't accidentally harm your own machine.

Dev plugins aren't verified or signed; that only matters when you publish to the store.

## Permissions

Only ask for what you use. Each one is shown to the user before they turn your plugin on. See the [plugin rulebook](../../docs/plugin-rules.md) for the full list.
