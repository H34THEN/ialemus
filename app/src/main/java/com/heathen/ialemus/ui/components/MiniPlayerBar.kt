package com.heathen.ialemus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun MiniPlayerBar(
    track: Track?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = if (track != null && isPlaying) {
        tokens.accentActive
    } else {
        tokens.hudBorderColor.copy(alpha = 0.5f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(tokens.panelOverlay.copy(alpha = 0.92f), MaterialTheme.shapes.medium)
            .border(1.5.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable(onClick = onOpenNowPlaying)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (track != null) "PLAYBACK CORE" else "AUDIO LINK",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (track != null) tokens.accentActive else tokens.textMuted,
            )
            Text(
                text = track?.title ?: "NO TRACK LOADED",
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track?.displayArtist ?: "Standby — open Library to select signal",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HudIconButton(
            icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            onClick = onPlayPause,
            enabled = track != null,
            highlighted = isPlaying,
        )
    }
}
