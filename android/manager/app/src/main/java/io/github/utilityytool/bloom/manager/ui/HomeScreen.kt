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
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

/**
 * Home is the version checker: it reads the installed Discord build and says whether
 * Bloom can be installed on it. The installer itself is a later piece.
 */
@Composable
fun HomeScreen(state: AppState) {
    val context = LocalContext.current
    var discord by remember { mutableStateOf(findDiscord(context)) }
    var showSoon by remember { mutableStateOf(false) }
    val canInstall = discord?.hasPhoenix == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BloomLockup(30.dp, Modifier.padding(top = 8.dp, bottom = 12.dp))
        }

        // The verdict: can Bloom be installed on this build?
        item {
            val found = discord
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomEnd = 32.dp, bottomStart = 12.dp),
                color = if (canInstall) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(if (canInstall) Icons.Default.CheckCircle else Icons.Default.Info, contentDescription = null)
                        Text(
                            when {
                                found == null -> "Discord isn't installed"
                                canInstall -> "Bloom can be installed"
                                else -> "Not this Discord build"
                            },
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    when {
                        found == null -> {
                            Text("Install Discord first, then check again.", style = MaterialTheme.typography.bodyMedium)
                            FilledTonalButton(onClick = { openLink(context, "market://details?id=com.discord") }) {
                                Text("Get Discord")
                            }
                        }
                        canInstall -> Text(
                            "Discord ${found.versionName} has Phoenix — the build Bloom needs.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        else -> Text(
                            "Discord ${found.versionName} doesn't have Phoenix. Bloom needs Discord's Phoenix (Alpha) build to run.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    TextButton(onClick = { discord = findDiscord(context); showSoon = false }, contentPadding = PaddingValues(0.dp)) {
                        Text("Check again")
                    }
                }
            }
        }

        // Install (enabled only when the build is compatible; the installer engine is next).
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Install Bloom", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (canInstall) "Your Discord build is compatible."
                        else "Grayed out until Bloom detects a compatible Discord build.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = { showSoon = true }, enabled = canInstall) { Text("Install Bloom") }
                    if (showSoon) {
                        Text(
                            "The installer is the next piece we're building — this is where it'll run.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Updates (what the Manager is for)
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Updates", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "The Manager keeps Bloom up to date and re-applies it after Discord updates. Nothing to update right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = {}) { Text("Check for updates") }
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
