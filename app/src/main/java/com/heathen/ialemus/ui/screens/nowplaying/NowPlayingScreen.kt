package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.EmptyLibraryState
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.StatusChip
import com.heathen.ialemus.ui.screens.queue.QueueSheet
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.albumArtUri
import com.heathen.ialemus.ui.util.formatDuration

@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.headlineMedium,
            color = tokens.glowColor,
            modifier = Modifier.fillMaxWidth(),
        )
        StatusChip(label = "LOCAL SIGNAL", highlighted = track != null)

        if (track == null) {
            EmptyLibraryState(
                title = "No track loaded",
                body = "Open Library, scan local music, and tap a track to start playback.",
            )
        } else {
            AlbumArtwork(track = track)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = track.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = track.displayArtist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Text(
                    text = track.displayAlbum,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            SeekBar(
                positionMs = playbackState.positionMs,
                durationMs = playbackState.durationMs,
                onSeek = playerViewModel::seekTo,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = playerViewModel::skipToPrevious) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = playerViewModel::playPause) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                    )
                }
                IconButton(onClick = playerViewModel::skipToNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    icon = { Icon(Icons.Filled.QueueMusic, contentDescription = null) },
                    label = "Queue",
                    onClick = { showQueue = true },
                )
                ActionChip(
                    icon = { Icon(Icons.Filled.Lyrics, contentDescription = null) },
                    label = "Lyrics",
                    onClick = { /* TODO(MVP 1B): local lyrics panel */ },
                )
                ActionChip(
                    icon = { Icon(Icons.Filled.GraphicEq, contentDescription = null) },
                    label = "Signal",
                    onClick = { /* TODO: output device / bitrate info */ },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        playerViewModel.toggleFavorite(track.id, !isFavorite)
                    },
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            HudPanel(title = "Signal Panel", subtitle = "Audio telemetry placeholder") {
                Text(
                    text = "Bitrate/sample rate HUD arrives later. Playback is local-only in MVP 1A hotfix.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }

            HudPanel(title = "Shuffle / Repeat", subtitle = "Mode: ${shuffleMode.displayName}") {}
        }

        // TODO: Landscape first-class three-pane HUD (MVP 5).
    }

    if (showQueue) {
        QueueSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showQueue = false },
        )
    }
}

@Composable
private fun AlbumArtwork(track: Track) {
    val artUri = track.albumArtUri()
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
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
                text = "No Art",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = positionMs.coerceIn(0L, safeDuration).toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..safeDuration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(positionMs), style = MaterialTheme.typography.bodySmall)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActionChip(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon,
    )
}
