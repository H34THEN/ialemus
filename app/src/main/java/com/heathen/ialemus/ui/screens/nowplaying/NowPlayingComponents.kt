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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heathen.ialemus.core.library.TrackTitleCleanup
import com.heathen.ialemus.core.model.QueueItem
import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlaybackState
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudIconButton
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.TrackRow
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.albumArtUri
import com.heathen.ialemus.ui.util.formatDuration
import java.text.DateFormat
import java.util.Date

data class NowPlayingUiState(
    val track: Track?,
    val playbackState: PlaybackState,
    val isFavorite: Boolean,
    val queueItems: List<QueueItem>,
)

@Composable
fun NowPlayingStatusRow(
    track: Track?,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier,
) {
    val playbackStatus = when {
        track == null -> "NO TRACK"
        playbackState.isBuffering -> "BUFFERING"
        playbackState.isPlaying -> "PLAYING"
        else -> "PAUSED"
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HudStatusChip(label = "LOCAL SIGNAL", highlighted = track != null)
        HudStatusChip(
            label = playbackStatus,
            highlighted = playbackState.isPlaying,
            warning = playbackState.isBuffering,
            disabled = track == null,
        )
    }
}

@Composable
fun NowPlayingArtworkPanel(
    track: Track,
    compact: Boolean,
    imageHeavy: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val artUri = track.albumArtUri()
    val aspect = when {
        imageHeavy -> if (compact) 0.82f else 0.95f
        compact -> 0.75f
        else -> 1f
    }
    val widthFraction = when {
        imageHeavy -> 1f
        compact -> 0.92f
        else -> 1f
    }
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .aspectRatio(aspect)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "◢ IALEMUS ◣",
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.glowColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "NO ART SIGNAL",
                    color = tokens.accentActive,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
fun NowPlayingTrackHeader(
    track: Track,
    centered: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val alignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Text(
            text = track.displayTitle,
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
            color = tokens.textPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = track.displayArtist,
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.glowColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = track.displayAlbum,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun NowPlayingSeekBar(
    playbackState: PlaybackState,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val safeDuration = playbackState.durationMs.coerceAtLeast(1L)
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = playbackState.positionMs.coerceIn(0L, safeDuration).toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..safeDuration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = tokens.accentActive,
                activeTrackColor = tokens.accentActive,
                inactiveTrackColor = tokens.hudBorderColor.copy(alpha = 0.35f),
            ),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(playbackState.positionMs), style = MaterialTheme.typography.labelSmall, color = tokens.textMuted)
            Text(formatDuration(playbackState.durationMs), style = MaterialTheme.typography.labelSmall, color = tokens.textMuted)
        }
    }
}

@Composable
fun NowPlayingTransportControls(
    playbackState: PlaybackState,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    highlightedPlay: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudIconButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = "Previous",
            onClick = playerViewModel::skipToPrevious,
            enabled = playbackState.canSkipPrevious,
        )
        HudIconButton(
            icon = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
            onClick = playerViewModel::playPause,
            highlighted = highlightedPlay,
        )
        HudIconButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = "Next",
            onClick = playerViewModel::skipToNext,
            enabled = playbackState.canSkipNext,
        )
    }
}

@Composable
fun NowPlayingActionRow(
    playbackState: PlaybackState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onToggleShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HudButton(
            label = "Queue (${playbackState.queueSize})",
            onClick = onOpenQueue,
            modifier = Modifier.weight(1f),
        )
        HudButton(
            label = if (playbackState.shuffleEnabled) "Shuffle On" else "Shuffle",
            onClick = onToggleShuffle,
            modifier = Modifier.weight(1f),
            accent = HudButtonAccent.Neutral,
        )
        HudIconButton(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Favorite",
            onClick = onToggleFavorite,
            highlighted = isFavorite,
        )
    }
}

@Composable
fun NowPlayingShuffleRepeatRow(
    playbackState: PlaybackState,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HudButton(
            label = if (playbackState.shuffleEnabled) "Shuffle On" else "Shuffle Off",
            onClick = playerViewModel::toggleShuffle,
            modifier = Modifier.weight(1f),
        )
        HudButton(
            label = playbackState.repeatMode.displayName,
            onClick = playerViewModel::cycleRepeat,
            modifier = Modifier.weight(1f),
            accent = HudButtonAccent.Warning,
        )
    }
}

@Composable
fun NowPlayingMetadataPanel(
    track: Track,
    playbackState: PlaybackState,
    isFavorite: Boolean,
    playCount: Int?,
    lastPlayedAt: Long?,
    expanded: Boolean,
    onToggle: () -> Unit,
    showTechnicalDetails: Boolean,
    onToggleTechnicalDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }

    HudCollapsiblePanel(
        title = "Local Signal",
        sectionTag = "METADATA",
        subtitle = track.sourceChipLabel,
        expanded = expanded,
        onToggle = onToggle,
        statusLabel = if (expanded) "OPEN" else "DETAILS",
        modifier = modifier,
    ) {
        MetadataLine("Title", track.displayTitle)
        MetadataLine("Scanned title", track.title)
        MetadataLine("Artist", track.displayArtist)
        MetadataLine("Album", track.displayAlbum)
        track.trackNumber?.let { MetadataLine("Track #", it.toString()) }
        track.discNumber?.let { MetadataLine("Disc #", it.toString()) }
        MetadataLine("Duration", formatDuration(track.durationMs))
        MetadataLine("Source type", sourceTypeLabel(track.sourceType))
        track.sourceLabel?.let { MetadataLine("Source label", it) }
        track.relativePath?.let { MetadataLine("Path", it) }
        track.size?.let { MetadataLine("File size", formatFileSize(it)) }
        if (track.dateAdded > 0L) MetadataLine("Date added", dateFormat.format(Date(track.dateAdded)))
        if (track.dateModified > 0L) MetadataLine("Date modified", dateFormat.format(Date(track.dateModified)))
        track.albumId?.let { MetadataLine("Album ID", it.toString()) }
        if (track.mediaStoreId > 0L) MetadataLine("MediaStore ID", track.mediaStoreId.toString())
        MetadataLine("Favorite", if (isFavorite) "Yes" else "No")
        playCount?.let { MetadataLine("Play count", it.toString()) }
        lastPlayedAt?.let { if (it > 0L) MetadataLine("Last played", dateFormat.format(Date(it))) }
        MetadataLine("Extension", track.contentUri.substringAfterLast('.', "unknown"))
        MetadataLine("Codec", "TODO — not scanned yet")
        MetadataLine("Bitrate", "TODO — not scanned yet")
        MetadataLine("Sample rate", "TODO — not scanned yet")
        MetadataLine("ReplayGain", "TODO — not scanned yet")
        MetadataLine(
            "Queue position",
            "${playbackState.queueIndex + 1} / ${playbackState.queueSize.coerceAtLeast(1)}",
        )

        HudButton(
            label = if (showTechnicalDetails) "Hide technical source details" else "Show technical source details",
            onClick = onToggleTechnicalDetails,
            accent = HudButtonAccent.Neutral,
        )
        if (showTechnicalDetails) {
            Text(
                text = track.contentUri,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
            )
        }
    }
}

@Composable
fun NowPlayingTrackCleanupPanel(
    track: Track,
    hasOverride: Boolean,
    onSaveOverride: (String) -> Unit,
    onResetOverride: () -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editTitle by remember(track.id, track.displayTitle) { mutableStateOf(track.displayTitle) }
    val suggested = remember(track.title) { TrackTitleCleanup.stripTrackNumberPrefix(track.title) }

    HudCollapsiblePanel(
        title = "Track Cleanup",
        sectionTag = "DISPLAY OVERRIDE",
        subtitle = "Change how Ialemus shows this track. Does not rename the file.",
        expanded = expanded,
        onToggle = onToggle,
        statusLabel = if (hasOverride) "OVERRIDE" else "EDIT",
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display title") },
            singleLine = true,
        )
        if (suggested != null && suggested != track.displayTitle) {
            Text(
                text = "Suggested: \"$suggested\"",
                style = MaterialTheme.typography.bodySmall,
                color = LocalIalemusTokens.current.glowColor,
            )
            HudButton(
                label = "Remove track number prefix",
                onClick = { editTitle = suggested },
                accent = HudButtonAccent.Neutral,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudButton(
                label = "Save display title",
                onClick = { onSaveOverride(editTitle) },
                modifier = Modifier.weight(1f),
            )
            if (hasOverride) {
                HudButton(
                    label = "Reset",
                    onClick = onResetOverride,
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Warning,
                )
            }
        }
        Text(
            text = "Future: rename file · edit embedded tags · write album/artist metadata (requires source write permission).",
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.textMuted,
        )
    }
}

@Composable
fun NowPlayingQueuePreview(
    queueItems: List<QueueItem>,
    onPlayQueueItem: (Int) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HudCollapsiblePanel(
        title = "Up Next",
        sectionTag = "QUEUE SYNC",
        subtitle = "${queueItems.size} tracks in queue",
        expanded = expanded,
        onToggle = onToggle,
        statusLabel = "${queueItems.size} TRACKS",
        modifier = modifier,
    ) {
        queueItems.take(12).forEach { item ->
            TrackRow(
                track = item.track,
                onClick = { onPlayQueueItem(item.queueIndex) },
                isPlaying = item.isCurrent,
                index = item.queueIndex,
            )
        }
        if (queueItems.size > 12) {
            Text(
                text = "+ ${queueItems.size - 12} more in full queue",
                style = MaterialTheme.typography.labelSmall,
                color = LocalIalemusTokens.current.textMuted,
            )
        }
        Text(
            text = "TODO: Radio mode · similar artist shuffle · smart queue generation",
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.textMuted,
        )
    }
}

@Composable
fun NowPlayingInactivePanel(onOpenLibrary: () -> Unit, modifier: Modifier = Modifier) {
    HudPanel(
        title = "Inactive Audio Core",
        sectionTag = "PLAYBACK CORE",
        subtitle = "No local signal linked. Choose a music folder in Library to begin.",
        modifier = modifier,
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
        }
    }
}

@Composable
private fun MetadataLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.textMuted,
            modifier = Modifier.weight(0.45f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = LocalIalemusTokens.current.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.55f),
            textAlign = TextAlign.End,
        )
    }
}

private fun sourceTypeLabel(type: SourceType): String = when (type) {
    SourceType.SAF_FOLDER -> "SAF folder"
    SourceType.MEDIASTORE_FULL_DEVICE -> "Full device scan"
    SourceType.LOCAL -> "Local library"
    SourceType.FUTURE_NAS, SourceType.NAS_INDEXED, SourceType.NAS_STREAM -> "NAS (future)"
    SourceType.DEVICE_CACHE -> "Device cache"
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    return String.format("%.1f MB", kb / 1024.0)
}
