package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlaybackState
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.formatDuration

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NowPlayingAudioToolsPanel(
    track: Track?,
    playbackState: PlaybackState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val speedPresets = listOf(0.75f, 1f, 1.25f, 1.5f)
    val sleepPresets = listOf(
        null to "Off",
        15 to "15 min",
        30 to "30 min",
        60 to "60 min",
        -1 to "End of track",
    )

    HudCollapsiblePanel(
        title = "Audio Tools",
        sectionTag = "SIGNAL",
        subtitle = "Playback speed, sleep timer, session readout.",
        expanded = expanded,
        onToggle = onToggle,
        statusLabel = "${playbackState.playbackSpeed}x",
        modifier = modifier,
    ) {
        Text(
            text = "Speed (pitch preserved by Media3 default)",
            style = MaterialTheme.typography.labelMedium,
            color = tokens.textPrimary,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            speedPresets.forEach { speed ->
                val active = playbackState.playbackSpeed == speed
                HudButton(
                    label = "${speed}x",
                    onClick = { onSetSpeed(speed) },
                    accent = if (active) HudButtonAccent.Primary else HudButtonAccent.Neutral,
                )
            }
        }

        Text(
            text = "Sleep timer",
            style = MaterialTheme.typography.labelMedium,
            color = tokens.textPrimary,
            modifier = Modifier.padding(top = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            sleepPresets.forEach { (minutes, label) ->
                val active = playbackState.sleepTimerMinutes == minutes ||
                    (minutes == -1 && playbackState.sleepTimerMinutes == -1)
                HudButton(
                    label = label,
                    onClick = { onSetSleepTimer(minutes) },
                    accent = if (active) HudButtonAccent.Warning else HudButtonAccent.Neutral,
                )
            }
        }
        playbackState.sleepTimerEndsAtMs?.let { endsAt ->
            val timeLabel = remember(endsAt) {
                java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(endsAt)
            }
            HudStatusChip(label = "STOPS AT $timeLabel", warning = true)
        }

        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Session", style = MaterialTheme.typography.labelMedium, color = tokens.textPrimary)
            AudioDetailLine("Position", formatDuration(playbackState.positionMs))
            AudioDetailLine("Duration", formatDuration(playbackState.durationMs))
            track?.let {
                AudioDetailLine("Format", it.contentUri.substringAfterLast('.', "unknown"))
            }
            AudioDetailLine("Bitrate", "Not scanned")
            AudioDetailLine("Sample rate", "Not scanned")
        }

        Text(
            text = "Future: normalize · EQ · replaygain · crossfade",
            style = MaterialTheme.typography.labelSmall,
            color = tokens.textMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun AudioDetailLine(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalIalemusTokens.current.textMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = LocalIalemusTokens.current.textPrimary)
    }
}
