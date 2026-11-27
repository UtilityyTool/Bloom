# Bloom plugin rules

The rulebook for anyone making a Bloom plugin. Bloom enforces most of these rules itself, so a plugin that breaks them simply won't work.

> **Status:** draft 1. The API names in the examples can still change before Bloom's first release.

## The short version

1. **Ask only for what you need.** Every permission is shown to the user before they turn your plugin on.
2. **Never act for the user.** No sending messages, joining servers or clicking things on their behalf.
3. **Stay fast.** Bloom measures every plugin. Slow plugins get warned, then paused.
4. **Keep data on the device** unless your plugin's job is to send it somewhere, and you say where.
5. **Write your own code.** Store plugins must be original or properly licensed.

---

## 1. What a plugin is

A plugin is a folder:

```
clean-links/
├─ plugin.json    who you are and what you need
├─ index.ts       your code
└─ README.md      what it does, in plain words
```

The same plugin runs on Windows and Android. Don't write platform-specific code unless you check `bloom.platform` first.

## 2. `plugin.json`

```json
{
  "id": "clean-links",
  "name": "Clean links",
  "version": "1.0.0",
  "author": "Your name",
  "description": "Strips tracking junk (like ?si= and utm_ tags) from links.",
  "bloom": ">=0.2.0",
  "permissions": ["messages.read", "messages.edit-outgoing"],
  "network": []
}
```

| Field | Required | What it's for |
| --- | --- | --- |
| `id` | yes | Lowercase letters, numbers and dashes. Never changes once published. |
| `name` | yes | What users see. 30 characters max. |
| `version` | yes | `major.minor.patch`. See [Updates](#9-updates). |
| `author` | yes | You. |
| `description` | yes | One or two sentences. Say what it does, not how cool it is. |
| `bloom` | yes | The oldest Bloom version your plugin works with. |
| `permissions` | yes | What your plugin needs. Can be empty: `[]`. |
| `network` | only if you talk to the internet | Exact website names, like `["translate.googleapis.com"]`. |

## 3. Permissions

Each permission has a **risk level**. Users see it as a color on the label, so medium and high permissions should be rare.

| Permission | Users see | What it allows | Risk |
| --- | --- | --- | --- |
| *(none)* | "No special access" | Settings page in the Bloom menu, logging to the console | none |
| `ui.change` | Changes Discord's look | Add, hide or restyle buttons and panels | low |
| `storage` | Saves data on this device | Keep up to 5 MB between restarts | low |
| `notifications` | Shows notifications | Pop up Bloom alerts | low |
| `clipboard.write` | Copies to your clipboard | Put text on the clipboard when the user taps something | low |
| `messages.read` | Reads messages | See messages in chats the user has open | medium |
| `messages.render` | Changes how messages look to you | Change how a message is *shown* on this device only. Others still see the original. | medium |
| `messages.edit-outgoing` | Edits messages you send | Change the user's message **after** they press send, **before** it goes out | medium |
| `network` (+ the list) | Sends data to *site.com* | Talk to the websites listed in `network`, and nothing else | high |

### Permission rules

- **Least access.** If you only change how links *look*, ask for `messages.render`, not `messages.edit-outgoing`.
- **Exact websites only.** `network` takes full names like `api.example.com`. Wildcards like `*.example.com` aren't allowed.
- **No surprises.** If an update adds a permission, Bloom turns the plugin off and asks the user again.
- **Say why.** Your README must explain why you need each medium or high permission, in one line each.

## 4. Never allowed

There's no permission for these. Bloom's runtime doesn't give plugins any way to do them.

| Never | Why |
| --- | --- |
| Reading the user's **password or login token** | Whoever has the token has the account. |
| Sending messages, reactions or friend requests **without the user pressing send** | Automating accounts is what gets people banned. |
| **Mass actions** (DM everyone, delete all, join many servers) | Same reason, and it hurts other people. |
| Making requests to Discord that the normal app wouldn't | Discord sees it on their servers. It puts the account at risk. |
| Reading **other plugins'** data or settings | Each plugin gets its own sealed box. |
| Running programs, reading files, or opening system settings | Plugins run in Bloom's sandbox, not on the device. |
| Hiding what the plugin does (obfuscated or minified code in the store) | Reviewers and users must be able to read it. |
| Ads, tracking or analytics | Bloom is for the user, not for data collection. |

## 5. Performance: Bloom must feel instant

Discord on a 120 Hz phone has about **8 milliseconds** to draw each frame. If a plugin takes longer, scrolling stutters. Bloom protects that budget.

### Time limits

| What | Limit | What Bloom does if you go over |
| --- | --- | --- |
| One event handler (for example, a new message) | **2 ms** normal, **8 ms** hard | Over 8 ms: a warning in the console |
| Same handler, repeatedly | Over 8 ms **5 times in a minute** | Plugin shows as **Slow** in the menu |
| Any single call | **50 ms** | Plugin is **paused** and the user is told |
| Starting up | **20 ms** | Startup is delayed until after Discord has drawn its first screen |
| Memory | **32 MB** per plugin | Plugin is paused |
| Network request | **10 s** timeout | The request fails, the plugin keeps running |

### How to stay fast

- **Do the small thing now and the big thing later.** Use `bloom.later(fn)` for anything that doesn't need to happen before the next frame.
- **Never wait on the network inside a handler.** Start the request, return right away, and update when the answer arrives.
- **Change the UI once per frame, not once per message.** Bloom batches your UI changes. Don't fight it by forcing updates.
- **Cache.** If you computed something for a message, don't recompute it every time it scrolls into view.
- **No loops that wait** (`while (!ready) {}`). They freeze the whole app.

### What users see

The Bloom menu shows **"Slow"** next to a plugin that keeps going over its limits, and Troubleshooting shows the exact numbers. Users should never have to guess which plugin is making Discord laggy.

## 6. Privacy

- Data **stays on the device** unless your plugin has `network` permission.
- If you send data anywhere, the README says **what** you send and **where**, in plain words.
- Store as little as you can, and delete it when the user turns your plugin off (Bloom asks first).
- Never sell, share or log user data on your own servers beyond what the feature needs.

## 7. Playing nice with Discord

- Plugins change what the user **sees** and help them do things **faster themselves**. They don't do things *for* them.
- Respect Discord's limits. If your feature would make lots of requests, it doesn't belong in a plugin.
- Hidden Discord experiments are off-limits to plugins. Only Bloom's Experimental settings, with their warning, can touch them.

## 8. Getting into the Bloom store

Every store plugin is read by a person before it's listed. They check:

- [ ] `plugin.json` is complete and the permissions match what the code actually uses
- [ ] The README explains every medium and high permission
- [ ] No rule from [Never allowed](#4-never-allowed) is broken
- [ ] It stays inside the [performance limits](#5-performance-bloom-must-feel-instant) on a mid-range phone
- [ ] The code is readable (not minified or obfuscated)
- [ ] The code is original, or its license allows it and it's credited (see [Originality](#10-originality-and-license))
- [ ] It works on both Windows and Android, or says clearly which one it's for

## 9. Updates

- **Patch** (`1.0.0` → `1.0.1`): bug fixes. Installs automatically.
- **Minor** (`1.0.0` → `1.1.0`): new features, same permissions. Installs automatically.
- **Major** (`1.0.0` → `2.0.0`): big changes or new permissions. The user is asked first.
- If Bloom's hooks change after a Discord update and your plugin needs one that's broken, Bloom **pauses** your plugin instead of letting it crash. It turns back on by itself when the hook is fixed.

## 10. Originality and license

- Bloom is licensed under the **GPL-3.0**. Plugins in the store must use a GPL-3.0-compatible license.
- Write your own code. Don't copy plugins from other Discord mods. If a plugin was inspired by another mod's *idea*, that's fine, but say so in the README.
- If you use someone else's open-source code, its license must allow it, and you credit them in the README.

## 11. A full example

```ts
// index.ts for "Clean links"
// Permissions: messages.read, messages.edit-outgoing

const TRACKERS = ["si", "utm_source", "utm_medium", "utm_campaign", "fbclid", "igsh"];

function clean(text: string): string {
  return text.replace(/https?:\/\/\S+/g, (link) => {
    try {
      const url = new URL(link);
      TRACKERS.forEach((t) => url.searchParams.delete(t));
      return url.toString();
    } catch {
      return link; // not a real link, leave it alone
    }
  });
}

export default bloom.plugin({
  start() {
    // Runs after the user presses send, before the message goes out.
    // Fast: one regex over one message, well under 2 ms.
    bloom.messages.beforeSend((message) => {
      message.content = clean(message.content);
    });
  },
});
```

## Words used here

- **Hook:** a spot inside Discord that Bloom connects to, like "a message arrived."
- **Handler:** your function that runs when a hook fires.
- **Sandbox:** the sealed space plugins run in. They can only use what Bloom hands them.
- **Runtime:** the part of Bloom that runs plugins and enforces these rules.
