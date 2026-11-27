package io.github.utilityytool.bloom.manager.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.data.AppState
import io.github.utilityytool.bloom.manager.data.BuiltInPlugins

@Composable
fun PluginsScreen(state: AppState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        item {
            Text("Plugins", style = MaterialTheme.typography.headlineLarge)
            Text(
                "${state.onCount} of ${BuiltInPlugins.size} on · every plugin shows what it can access",
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(BuiltInPlugins, key = { it.id }) { plugin ->
            PluginRow(
                plugin = plugin,
                checked = state.plugins[plugin.id] == true,
                onCheckedChange = { state.setPlugin(plugin.id, it) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        }
    }
}
