package io.github.utilityytool.bloom.manager.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.data.AppState
import io.github.utilityytool.bloom.manager.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: AppState) {
    val context = LocalContext.current
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "?"
    val canUseWallpaper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineLarge) }

        item {
            SectionTitle("Look")
            val modes = listOf(ThemeMode.SYSTEM to "Phone setting", ThemeMode.DARK to "Dark", ThemeMode.LIGHT to "Light")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                modes.forEachIndexed { i, (mode, label) ->
                    SegmentedButton(
                        selected = state.themeMode == mode,
                        onClick = { state.updateThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(i, modes.size),
                    ) { Text(label) }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Wallpaper colors", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (canUseWallpaper) "Match the app's colors to your wallpaper." else "Needs Android 12 or newer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.wallpaperColors && canUseWallpaper,
                    onCheckedChange = { state.updateWallpaperColors(it) },
                    enabled = canUseWallpaper,
                )
            }
        }

        item {
            SectionTitle("About")
            Text("Bloom Manager $version", style = MaterialTheme.typography.titleMedium)
            Text(
                "Test build. Not made by or affiliated with Discord. Client mods go against Discord's terms of service.",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = { openLink(context, "https://github.com/UtilityyTool/Bloom") },
                contentPadding = PaddingValues(0.dp),
            ) { Text("Bloom on GitHub") }
        }
    }
}
