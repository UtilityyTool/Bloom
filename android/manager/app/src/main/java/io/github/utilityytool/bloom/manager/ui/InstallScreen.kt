package io.github.utilityytool.bloom.manager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.utilityytool.bloom.manager.data.findDiscord

// Discord's own support page for joining the Android Alpha. We send people here;
// Bloom never hosts Discord's app itself.
private const val DISCORD_ALPHA_URL = "https://support.discord.com/hc/en-us/articles/360035675191"

private enum class Step { DETECT, GET_ALPHA, UNLOCK, DONE }

/**
 * Path B setup: get the user onto Discord's Alpha, then (later) unlock Phoenix.
 * The walkthrough and every button here work today. The actual unlock engine
 * is the next thing we build, so that one action is clearly marked as pending.
 */
@Composable
fun InstallScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val discord = remember { findDiscord(context) }
    var step by rememberSaveable { mutableStateOf(Step.DETECT) }
    var agreed by rememberSaveable { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Top bar: logo + step dots + close
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BloomLockup(22.dp)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    (1..3).forEach { i -> StepDot(i, step.ordinal + 1) }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close setup")
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    Step.DETECT -> DetectStep(discordVersion = discord?.versionName, isAlpha = discord?.isAlpha == true)
                    Step.GET_ALPHA -> GetAlphaStep(onOpenSignup = { openLink(context, DISCORD_ALPHA_URL) })
                    Step.UNLOCK -> UnlockStep(
                        agreed = agreed,
                        onToggle = { agreed = !agreed },
                        onUnlock = { if (agreed) step = Step.DONE },
                    )
                    Step.DONE -> DoneStep(onOpenDiscord = {
                        val launch = discord?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }
                        if (launch != null) runCatching { context.startActivity(launch) }
                        onClose()
                    })
                }
            }

            // Bottom bar: Back (steps 2-3) + the main advance button (steps 1-2)
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step == Step.GET_ALPHA || step == Step.UNLOCK) {
                    OutlinedButton(
                        onClick = { step = Step.entries[step.ordinal - 1]; agreed = false },
                        modifier = Modifier.height(52.dp),
                    ) { Text("Back") }
                }
                when (step) {
                    Step.DETECT -> MainButton("Start", Modifier.weight(1f)) { step = Step.GET_ALPHA }
                    Step.GET_ALPHA -> MainButton("I've got the Alpha", Modifier.weight(1f)) { step = Step.UNLOCK; agreed = false }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun MainButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(52.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun StepDot(index: Int, current: Int) {
    val done = current > index
    val active = current == index
    val bg = when {
        done -> MaterialTheme.colorScheme.primary
        active -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val fg = if (done || active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = RoundedCornerShape(14.dp), color = bg, modifier = Modifier.size(28.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(if (done) "✓" else index.toString(), color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) { content() }
}

@Composable
private fun InfoCard(tinted: Boolean = false, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (tinted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (tinted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { content() }
    }
}

@Composable
private fun DetectStep(discordVersion: String?, isAlpha: Boolean) {
    StepColumn {
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { BloomMark(96.dp) }
        Text("Let's set up Bloom", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())
        Text(
            "Bloom runs on Discord's new app (Phoenix). We'll help you switch over. It takes a few minutes.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    when {
                        discordVersion == null -> "Discord isn't installed yet."
                        isAlpha -> "Found: Discord $discordVersion (Alpha). Good — this build has Phoenix inside."
                        else -> "Found: Discord $discordVersion. Phoenix isn't in this build yet."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        InfoCard(tinted = true) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Text(
                "Use a spare Discord account, not your main. Turning on Phoenix early carries some account risk.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun GetAlphaStep(onOpenSignup: () -> Unit) {
    StepColumn {
        Text("Get Discord's Alpha", style = MaterialTheme.typography.headlineMedium)
        Text(
            "This comes straight from Discord and Google. Bloom never hands you a copy of Discord itself.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                NumberedRow(1, "Tap the button below to open Discord's own Alpha sign-up page.")
                NumberedRow(2, "Sign in with your spare account and join the Alpha.")
                NumberedRow(3, "The Play Store updates Discord to the Alpha. Come back here when it's done.")
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onOpenSignup,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) { Text("Open Discord's Alpha sign-up", style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
private fun NumberedRow(n: Int, text: String) {
    Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(26.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(n.toString(), color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun UnlockStep(agreed: Boolean, onToggle: () -> Unit, onUnlock: () -> Unit) {
    StepColumn {
        Text("Turn on Phoenix", style = MaterialTheme.typography.headlineMedium)
        Text(
            "You have the Alpha, but Discord keeps Phoenix locked. This is where Bloom unlocks it on your account.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(checked = agreed, onCheckedChange = { onToggle() })
            Text(
                "Phoenix is staff-only for now, so unlocking it early has some account risk. Got it.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onUnlock,
            enabled = agreed,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
            Text("Unlock Phoenix", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "Note: the unlock engine isn't built yet, so this walkthrough stops here for now. This screen is where it will run.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DoneStep(onOpenDiscord: () -> Unit) {
    StepColumn {
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(48.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(52.dp))
                }
            }
        }
        Text("Walkthrough complete", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())
        Text(
            "The Alpha is set up. Turning on Phoenix is the next piece we're building, and it'll happen right in this flow.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        InfoCard {
            Text(
                "When Discord releases Phoenix for everyone, Bloom drops this whole step automatically. Nothing for you to do.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onOpenDiscord, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Open Discord", style = MaterialTheme.typography.titleMedium)
        }
    }
}
