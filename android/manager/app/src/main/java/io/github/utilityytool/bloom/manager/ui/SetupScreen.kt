package io.github.utilityytool.bloom.manager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.data.BuiltInPlugins
import io.github.utilityytool.bloom.manager.data.RecommendedIds
import io.github.utilityytool.bloom.manager.data.StartPreset

/** First-run setup: pick how to start, see exactly what you're getting, finish. */
@Composable
fun SetupScreen(onFinish: (StartPreset, Map<String, Boolean>) -> Unit) {
    var preset by rememberSaveable { mutableStateOf(StartPreset.RECOMMENDED) }
    val custom = remember {
        mutableStateMapOf<String, Boolean>().apply {
            BuiltInPlugins.forEach { put(it.id, it.id in RecommendedIds) }
        }
    }
    val shown = when (preset) {
        StartPreset.RECOMMENDED -> BuiltInPlugins.filter { it.id in RecommendedIds }
        StartPreset.MENU_ONLY -> emptyList()
        StartPreset.CUSTOM -> BuiltInPlugins
    }

    Scaffold(
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Button(
                    onClick = { onFinish(preset, custom.toMap()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(56.dp),
                ) { Text("Finish setup", style = MaterialTheme.typography.titleMedium) }
            }
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BloomMark(72.dp)
                    Text("Welcome to Bloom", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "Plugins and extra features for Discord's new app. Everything lives in one menu, so Discord itself looks the same.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("How do you want to start?", style = MaterialTheme.typography.titleLarge)
                }
            }
            items(StartPreset.entries) { p ->
                ChoiceCard(selected = p == preset, onClick = { preset = p }) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(p.tag, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(p.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                SectionTitle(if (preset == StartPreset.CUSTOM) "Turn on what you want" else "What you get")
                if (preset == StartPreset.MENU_ONLY) {
                    Text(
                        "Nothing extra. The Bloom menu will be there when you want plugins.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(shown, key = { it.id }) { plugin ->
                PluginRow(
                    plugin = plugin,
                    checked = if (preset == StartPreset.CUSTOM) custom[plugin.id] == true else null,
                    onCheckedChange = { custom[plugin.id] = it },
                )
            }
            item {
                Surface(
                    modifier = Modifier.padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Text(
                            "Client mods go against Discord's terms of service. Bloom never asks for your password or login token.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
