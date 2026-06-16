package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudIconButton
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.screens.queue.QueueSheet
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.albumArtUri
import com.heathen.ialemus.ui.util.formatDuration

@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val shuffleMode by playerViewModel.shuffleMode.collectAsStateWithLifecycle()
    val track = playbackState.currentTrack
    var showQueue by remember { mutableStateOf(false) }
    val isFavorite by playerViewModel
        .observeFavorite(track?.id.orEmpty())
        .collectAsStateWithLifecycle(initialValue = false)

    val playbackStatus = when {
        track == null -> "NO TRACK"
        playbackState.isBuffering -> "BUFFERING"
        playbackState.isPlaying -> "PLAYING"
        else -> "PAUSED"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HudHeader(
            title = "Now Playing",
            statusLabel = "PLAYBACK CORE",
            subtitle = "LOCAL SIGNAL · DAP COMMAND CONSOLE",
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudStatusChip(label = "LOCAL SIGNAL", highlighted = track != null)
            HudStatusChip(
                label = playbackStatus,
                highlighted = playbackState.isPlaying,
                warning = playbackState.isBuffering,
                disabled = track == null,
            )
        }

        if (track == null) {
            InactiveAudioCorePanel(onOpenLibrary = onOpenLibrary)
        } else {
            AlbumArtModule(track = track)
            TrackMetadataPanel(track = track)

            HudPanel(title = "Seek Control", sectionTag = "AUDIO LINK") {
                SeekBar(
                    positionMs = playbackState.positionMs,
                    durationMs = playbackState.durationMs,
                    onSeek = playerViewModel::seekTo,
                )
            }

            HudPanel(title = "Transport Cluster", sectionTag = "QUEUE SYNC") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HudIconButton(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        onClick = playerViewModel::skipToPrevious,
                    )
                    HudIconButton(
                        icon = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        onClick = playerViewModel::playPause,
                        highlighted = true,
                    )
                    HudIconButton(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        onClick = playerViewModel::skipToNext,
                    )
                }
            }

            HudButton(
                label = "Queue",
                onClick = { showQueue = true },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HudButton(
                    label = "Lyrics",
                    onClick = { /* TODO(MVP 1B): local lyrics panel */ },
                    modifier = Modifier.weight(1f),
                    accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
                )
                HudButton(
                    label = "Signal",
                    onClick = { /* TODO: output device / bitrate info */ },
                    modifier = Modifier.weight(1f),
                    accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
                )
                HudIconButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    onClick = { playerViewModel.toggleFavorite(track.id, !isFavorite) },
                    highlighted = isFavorite,
                )
            }

            HudPanel(
                title = "Audio Core Metadata",
                sectionTag = "LOCAL SIGNAL",
                subtitle = "Queue ${playbackState.queueIndex + 1} of ${playbackState.queueSize.coerceAtLeast(1)}",
            ) {
                Text(
                    text = "Source: ${track.sourceChipLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textMuted,
                )
                Text(
                    text = "Duration: ${formatDuration(track.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            HudPanel(
                title = "Shuffle / Repeat",
                sectionTag = "DAP MODE",
                subtitle = "Mode: ${shuffleMode.displayName}",
            ) {}
        }
    }

    if (showQueue) {
        QueueSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showQueue = false },
        )
    }
}

@Composable
private fun InactiveAudioCorePanel(onOpenLibrary: () -> Unit) {
    HudPanel(
        title = "Inactive Audio Core",
        sectionTag = "PLAYBACK CORE",
        subtitle = "No local signal linked. Approve a music folder in Library, scan sources, then tap a track to begin playback.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .background(
                        LocalIalemusTokens.current.surfaceDeep.copy(alpha = 0.8f),
                        MaterialTheme.shapes.medium,
                    )
                    .border(
                        1.dp,
                        LocalIalemusTokens.current.hudBorderColor.copy(alpha = 0.4f),
                        MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = LocalIalemusTokens.current.textMuted,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "AUDIO CORE OFFLINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalIalemusTokens.current.warningColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            HudButton(label = "Open Library", onClick = onOpenLibrary)
            HudButton(
                label = "Choose Music Folder",
                onClick = onOpenLibrary,
            )
            Text(
                text = "In Library: SOURCE SELECT → Choose Music Folder → Scan Selected Folders",
                style = MaterialTheme.typography.bodySmall,
                color = LocalIalemusTokens.current.textMuted,
            )
        }
    }
}

@Composable
private fun AlbumArtModule(track: Track) {
    val tokens = LocalIalemusTokens.current
    val artUri = track.albumArtUri()
    HudPanel(title = "Display Module", sectionTag = "TRACK INDEX") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, tokens.accentActive.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                .background(tokens.surfaceDeep, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            if (artUri != null) {
                AsyncImage(
                    model = artUri,
                    contentDescription = "Album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = "NO ART SIGNAL",
                    color = tokens.textMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun TrackMetadataPanel(track: Track) {
    val tokens = LocalIalemusTokens.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = track.title,
            style = MaterialTheme.typography.headlineSmall,
            color = tokens.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = track.displayArtist,
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.glowColor,
        )
        Text(
            text = track.displayAlbum,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.textMuted,
        )
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    val tokens = LocalIalemusTokens.current
    val safeDuration = durationMs.coerceAtLeast(1L)
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = positionMs.coerceIn(0L, safeDuration).toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..safeDuration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = tokens.accentActive,
                activeTrackColor = tokens.accentActive,
                inactiveTrackColor = tokens.hudBorderColor.copy(alpha = 0.35f),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(positionMs), style = MaterialTheme.typography.labelSmall, color = tokens.textMuted)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.labelSmall, color = tokens.textMuted)
        }
    }
}
