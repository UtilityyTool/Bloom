package io.github.utilityytool.bloom.manager.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.utilityytool.bloom.manager.ui.theme.ThemeMode

/**
 * Everything the app remembers. Each change is saved to the phone right away, and the
 * screens update automatically because the values are Compose state.
 */
class AppState(context: Context) {
    private val prefs = context.getSharedPreferences("bloom", Context.MODE_PRIVATE)

    var setupDone by mutableStateOf(prefs.getBoolean("setup_done", false))
        private set

    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme_mode", null) ?: "") }
            .getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var wallpaperColors by mutableStateOf(prefs.getBoolean("wallpaper_colors", true))
        private set

    /** Plugin id -> on/off. */
    val plugins = mutableStateMapOf<String, Boolean>().apply {
        BuiltInPlugins.forEach { put(it.id, prefs.getBoolean("plugin_${it.id}", false)) }
    }

    val onCount: Int get() = plugins.values.count { it }

    fun finishSetup(preset: StartPreset, custom: Map<String, Boolean>) {
        val edit = prefs.edit()
        BuiltInPlugins.forEach { p ->
            val on = when (preset) {
                StartPreset.RECOMMENDED -> p.id in RecommendedIds
                StartPreset.MENU_ONLY -> false
                StartPreset.CUSTOM -> custom[p.id] == true
            }
            plugins[p.id] = on
            edit.putBoolean("plugin_${p.id}", on)
        }
        edit.putBoolean("setup_done", true).apply()
        setupDone = true
    }

    fun setPlugin(id: String, on: Boolean) {
        plugins[id] = on
        prefs.edit().putBoolean("plugin_$id", on).apply()
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun updateWallpaperColors(on: Boolean) {
        wallpaperColors = on
        prefs.edit().putBoolean("wallpaper_colors", on).apply()
    }

    fun runSetupAgain() {
        setupDone = false
        prefs.edit().putBoolean("setup_done", false).apply()
    }
}
