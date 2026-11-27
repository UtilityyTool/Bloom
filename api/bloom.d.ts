// Bloom plugin API — TypeScript types for plugin authors.
//
// These match the `bloom` global the runtime exposes, so a plugin written against
// these types will actually run. Reference it from your plugin:
//
//   /// <reference path="../../api/bloom.d.ts" />
//
// Every call that needs a permission says so. Declare that permission in your
// plugin.json, or the call throws at runtime.

declare const bloom: Bloom.Api;

declare namespace Bloom {
  /** Events a plugin can react to. */
  type Event = "message" | "beforeSend" | "uiReady";

  /** What a handler receives. Fields are present only when your permissions allow them. */
  interface EventContext {
    /** The message text, when your plugin may see it. */
    text?: string;
  }

  interface Api {
    /** Register a handler for an event. Call this in your plugin's top level. */
    on(event: Event, handler: (e: EventContext) => void): void;

    /** Write a line to the Bloom console. Always allowed. */
    log(message: unknown): void;

    /** The incoming message's text. Requires the `messages.read` permission. */
    getMessageText(): string;

    /** The text you're about to send. Requires the `messages.edit-outgoing` permission. */
    getOutgoingText(): string;

    /** Change the text before it sends. Requires the `messages.edit-outgoing` permission. */
    setOutgoingText(text: string): void;

    /** Per-plugin storage on the device. Requires the `storage` permission. */
    readonly storage: Storage;
  }

  /** A small key/value store, private to your plugin, capped at 5 MB. */
  interface Storage {
    /** The value for `key`, or null if unset. */
    get(key: string): string | null;
    /** Save a value (both key and value are stored as strings). */
    set(key: string, value: string): void;
    /** Remove a key. */
    remove(key: string): void;
    /** Every key you've stored. */
    keys(): string[];
  }
}
