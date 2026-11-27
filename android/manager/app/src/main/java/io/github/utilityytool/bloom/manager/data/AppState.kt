package io.github.utilityytool.bloom.manager.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.utilityytool.bloom.manager.ui.theme.ThemeMode

/**
 * What the Manager app remembers. The Manager handles installing, updating and
 * settings — plugins themselves live inside Discord, not here. Each change saves
 * to the phone right away, and the screens update because these are Compose state.
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

    fun finishSetup() {
        setupDone = true
        prefs.edit().putBoolean("setup_done", true).apply()
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
