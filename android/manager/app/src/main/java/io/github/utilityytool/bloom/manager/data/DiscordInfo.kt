package io.github.utilityytool.bloom.manager.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/** What we found out about a Discord app installed on this phone. */
data class DiscordBuild(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    /** An Alpha build. Discord ships the new Rust client (Phoenix) inside its Android Alpha. */
    val isAlpha: Boolean,
    /**
     * True when this build can run Phoenix: it's an Alpha build, or it's a
     * separately-installed Phoenix package. This is the signal the setup checks for.
     */
    val hasPhoenix: Boolean,
)

// The regular app, and the package a side-by-side Phoenix build installs under.
private val DISCORD_PACKAGES = listOf("com.discord", "com.discord.phoenix")

private fun readPackage(context: Context, pkg: String): DiscordBuild? {
    val pm = context.packageManager
    val info: PackageInfo = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        return null
    }

    val version = info.versionName ?: "unknown"
    val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        info.versionCode.toLong()
    }
    // Discord's own support docs say the way to tell builds apart is the version string.
    val isAlpha = version.contains("alpha", ignoreCase = true)
    val hasPhoenix = isAlpha || pkg == "com.discord.phoenix"
    return DiscordBuild(pkg, version, code, isAlpha, hasPhoenix)
}

/**
 * Looks across every Discord package on the phone and returns the best one:
 * a Phoenix-capable build if there is one, otherwise whatever Discord is installed,
 * otherwise null.
 *
 * Note: from outside Discord we can only read the installed package, not whether
 * Phoenix actually launches for this account. So [DiscordBuild.hasPhoenix] means
 * "the build that can run Phoenix is installed," not "Phoenix is already unlocked."
 */
fun findDiscord(context: Context): DiscordBuild? {
    val found = DISCORD_PACKAGES.mapNotNull { readPackage(context, it) }
    return found.firstOrNull { it.hasPhoenix } ?: found.firstOrNull()
}
