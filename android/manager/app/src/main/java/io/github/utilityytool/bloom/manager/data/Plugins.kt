package io.github.utilityytool.bloom.manager.data

/**
 * A plugin as the Manager shows it. Later this comes from each plugin's plugin.json;
 * for the test build it's a fixed list.
 */
data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    /** What the plugin is allowed to touch, in plain words. Shown on every plugin. */
    val access: String,
    val initials: String,
)

val BuiltInPlugins = listOf(
    PluginInfo("clean-links", "Clean links", "Strips tracking junk from links you send and get.", "Reads messages", "CL"),
    PluginInfo("better-embeds", "Better embeds", "Fixes X, Instagram and TikTok link previews.", "Edits links you send", "BE"),
    PluginInfo("quiet-composer", "Quiet composer", "Hides the Gift and Sticker buttons.", "Changes Discord's look", "QC"),
    PluginInfo("message-history", "Message history", "Keeps edited and deleted messages visible to you.", "Reads messages", "MH"),
    PluginInfo("translate", "Translate", "Translate any message with one tap.", "Sends text to Google", "Tr"),
    PluginInfo("latency-badge", "Latency badge", "Flags messages that were slow to send.", "Reads messages", "LB"),
)

/** The three ways to start, same as the Windows installer. */
enum class StartPreset(val title: String, val tag: String, val description: String) {
    RECOMMENDED("Recommended", "3 plugins", "Small fixes most people want."),
    MENU_ONLY("Just the menu", "Nothing on", "Add plugins later, when you want."),
    CUSTOM("Pick myself", "Custom", "Choose each plugin yourself."),
}

val RecommendedIds = setOf("clean-links", "better-embeds", "quiet-composer")
