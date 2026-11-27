package io.github.utilityytool.bloom.manager.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.data.AppState
import io.github.utilityytool.bloom.manager.data.findDiscord

@Composable
fun HomeScreen(state: AppState, onOpenPlugins: () -> Unit) {
    val context = LocalContext.current
    var discord by remember { mutableStateOf(findDiscord(context)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BloomMark(44.dp)
                Text("Bloom", style = MaterialTheme.typography.headlineLarge)
            }
        }

        // 1. Is Discord on this phone?
        item {
            val found = discord
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomEnd = 32.dp, bottomStart = 12.dp),
                color = if (found != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(if (found != null) Icons.Default.CheckCircle else Icons.Default.Info, contentDescription = null)
                        Text(
                            if (found != null) "Discord found" else "Discord isn't installed",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    if (found != null) {
                        Text("Version ${found.versionName}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (found.isAlpha) "This is an alpha build, which has Discord's new Phoenix app inside it."
                            else "Phoenix isn't in regular Discord yet. For now it's only in Discord's alpha builds.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        Text("Install Discord first, then come back here.", style = MaterialTheme.typography.bodyMedium)
                        FilledTonalButton(onClick = { openLink(context, "market://details?id=com.discord") }) {
                            Text("Get Discord")
                        }
                    }
                    TextButton(onClick = { discord = findDiscord(context) }, contentPadding = PaddingValues(0.dp)) {
                        Text("Check again")
                    }
                }
            }
        }

        // 2. Installing Bloom into Discord (next milestone).
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Bloom to Discord", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Bloom Manager will make a copy of Discord with Bloom inside and install it next to the regular app. This part isn't built yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = {}, enabled = false) { Text("Coming soon") }
                }
            }
        }

        // 3. Plugins summary.
        item {
            Surface(
                onClick = onOpenPlugins,
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${state.onCount} plugins ready", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "They'll turn on once Bloom is in Discord.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    @Suppress("DEPRECATION")
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open plugins")
                }
            }
        }
    }
}

fun openLink(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
