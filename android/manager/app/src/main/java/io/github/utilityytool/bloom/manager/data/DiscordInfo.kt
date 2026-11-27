package io.github.utilityytool.bloom.manager.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** What we know about the Discord app on this phone. */
data class DiscordApp(
    val packageName: String,
    val versionName: String,
    /** Alpha builds are the ones that currently ship the new Phoenix client inside them. */
    val isAlpha: Boolean,
)

/** Looks for the official Discord app. Returns null if it isn't installed. */
fun findDiscord(context: Context): DiscordApp? {
    val pm = context.packageManager
    val pkg = "com.discord"
    return try {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0)
        }
        val version = info.versionName ?: "unknown"
        DiscordApp(pkg, version, isAlpha = version.contains("alpha", ignoreCase = true))
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}
