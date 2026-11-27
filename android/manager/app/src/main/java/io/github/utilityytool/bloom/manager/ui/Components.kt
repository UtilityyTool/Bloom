package io.github.utilityytool.bloom.manager.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.data.PluginInfo

/** The Bloom flower logo, drawn in the current theme's colors. */
@Composable
fun BloomMark(size: Dp, modifier: Modifier = Modifier) {
    val petal = MaterialTheme.colorScheme.primary
    val center = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension * 0.19f
        val c = this.size.minDimension / 2f
        val d = this.size.minDimension * 0.23f
        listOf(Offset(c, c - d), Offset(c + d, c), Offset(c, c + d), Offset(c - d, c)).forEach {
            drawCircle(petal, r, it)
        }
        drawCircle(center, r * 0.62f, Offset(c, c))
    }
}

/** Rounded square with a plugin's initials, used as its icon. */
@Composable
fun PluginBadge(plugin: PluginInfo, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(plugin.initials, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/** Small tinted label, e.g. "Reads messages". */
@Composable
fun AccessLabel(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** One plugin: badge, name, description, what it can access, and an optional switch. */
@Composable
fun PluginRow(
    plugin: PluginInfo,
    checked: Boolean?,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PluginBadge(plugin)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(plugin.name, style = MaterialTheme.typography.titleMedium)
            Text(
                plugin.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AccessLabel(plugin.access)
        }
        if (checked != null) {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

/**
 * A choice card whose corners spring into a bolder shape when picked:
 * a small Material 3 Expressive touch.
 */
@Composable
fun ChoiceCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val bouncy = spring<Dp>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    val big by animateDpAsState(if (selected) 32.dp else 20.dp, bouncy, label = "corner")
    val small by animateDpAsState(if (selected) 10.dp else 20.dp, bouncy, label = "cornerSmall")
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = big, topEnd = big, bottomEnd = big, bottomStart = small),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
    ) {
        Box(Modifier.padding(18.dp)) { content() }
    }
}

/** A section heading inside a screen. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}
