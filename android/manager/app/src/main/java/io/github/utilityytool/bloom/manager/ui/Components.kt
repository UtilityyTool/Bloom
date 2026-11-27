package io.github.utilityytool.bloom.manager.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.R

/** The Bloom logo ("Bud"), with its violet-to-pink gradient. */
@Composable
fun BloomMark(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.bloom_mark),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

/** "Bloom" in rounded Google Sans Flex, drawn as a vector so it looks the same everywhere. */
@Composable
fun BloomWordmark(height: Dp, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Image(
        painter = painterResource(R.drawable.bloom_wordmark),
        contentDescription = "Bloom",
        colorFilter = ColorFilter.tint(color),
        modifier = modifier.height(height).aspectRatio(WORDMARK_RATIO),
    )
}

/** Logo and wordmark side by side, spaced the same way as on the Brand page. */
@Composable
fun BloomLockup(height: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(height * 0.38f),
    ) {
        BloomMark(height * 1.45f)
        BloomWordmark(height)
    }
}

private const val WORDMARK_RATIO = 4.65f

/**
 * A card whose corners spring into a bolder shape when picked:
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
