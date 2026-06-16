package com.heathen.ialemus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.ui.theme.CompactLayout
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.albumArtUri

@Composable
fun MiniPlayerBar(
    track: Track?,
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (track == null) return

    val tokens = LocalIalemusTokens.current
    val borderColor = if (isPlaying) tokens.accentActive else tokens.hudBorderColor.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(tokens.panelOverlay.copy(alpha = 0.94f), MaterialTheme.shapes.medium)
            .border(1.5.dp, borderColor, MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenNowPlaying),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiniArtThumbnail(track = track)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PLAYBACK CORE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tokens.accentActive,
                        maxLines = 1,
                    )
                    Text(
                        text = track.displayTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = track.displayArtist,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniTransportButton(
                icon = Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                onClick = onToggleShuffle,
                active = shuffleEnabled,
            )
            MiniTransportButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                onClick = onPrevious,
                enabled = canSkipPrevious,
            )
            MiniTransportButton(
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = onPlayPause,
                highlighted = true,
            )
            MiniTransportButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = "Next",
                onClick = onNext,
                enabled = canSkipNext,
            )
            MiniTransportButton(
                icon = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Filled.RepeatOne
                    RepeatMode.QUEUE -> Icons.Filled.Repeat
                    RepeatMode.OFF -> Icons.Filled.Repeat
                },
                contentDescription = "Repeat ${repeatMode.displayName}",
                onClick = onCycleRepeat,
                active = repeatMode != RepeatMode.OFF,
            )
        }
    }
}

@Composable
private fun MiniArtThumbnail(track: Track) {
    val tokens = LocalIalemusTokens.current
    val artUri = track.albumArtUri()
    Box(
        modifier = Modifier
            .size(CompactLayout.miniPlayerArtSize)
            .border(1.dp, tokens.hudBorderColor, MaterialTheme.shapes.small)
            .background(tokens.surfaceDeep, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        if (artUri != null) {
            AsyncImage(
                model = artUri,
                contentDescription = "Album art",
                modifier = Modifier.size(CompactLayout.miniPlayerArtSize),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = tokens.textMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun MiniTransportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    highlighted: Boolean = false,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = when {
        !enabled -> tokens.hudBorderColor.copy(alpha = 0.2f)
        highlighted || active -> tokens.accentActive
        else -> tokens.hudBorderColor.copy(alpha = 0.45f)
    }
    val tint = when {
        !enabled -> tokens.textMuted.copy(alpha = 0.4f)
        highlighted || active -> tokens.accentActive
        else -> tokens.glowColor
    }
    Box(
        modifier = modifier
            .size(CompactLayout.miniTransportButtonSize)
            .alpha(if (enabled) 1f else 0.5f)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(CompactLayout.miniTransportIconSize),
        )
    }
}
