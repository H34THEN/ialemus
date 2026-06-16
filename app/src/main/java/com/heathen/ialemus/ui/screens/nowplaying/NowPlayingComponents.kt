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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heathen.ialemus.core.library.TrackTitleCleanup
import com.heathen.ialemus.data.local.entity.TrackOverrideEntity
import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.QueueItem
import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlaybackState
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudIconButton
import com.heathen.ialemus.ui.components.HudOutlinedTextField
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
        imageHeavy -> if (compact) 0.72f else 0.88f
        compact -> 1f
        else -> 1f
    }
    val widthFraction = when {
        imageHeavy -> if (compact) 0.88f else 1f
        compact -> 0.72f
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
fun NowPlayingCompactArtwork(
    track: Track,
    modifier: Modifier = Modifier,
    sizeDp: androidx.compose.ui.unit.Dp = 72.dp,
) {
    val tokens = LocalIalemusTokens.current
    val artUri = track.albumArtUri()
    Box(
        modifier = modifier
            .size(sizeDp)
            .border(1.5.dp, tokens.accentActive.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            .background(tokens.surfaceDeep, MaterialTheme.shapes.small),
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
                text = "◢",
                style = MaterialTheme.typography.labelLarge,
                color = tokens.glowColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun NowPlayingPrimaryControls(
    playbackState: PlaybackState,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NowPlayingIconControls(
        playbackState = playbackState,
        onToggleShuffle = onToggleShuffle,
        onPrevious = onPrevious,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onCycleRepeat = onCycleRepeat,
        modifier = modifier,
    )
}

@Composable
fun NowPlayingTextMetadataHeader(
    track: Track,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NowPlayingCompactArtwork(track = track)
        Column(modifier = Modifier.weight(1f)) {
            NowPlayingTrackHeader(track = track, centered = false, compact = false)
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
    NowPlayingIconControls(
        playbackState = playbackState,
        onToggleShuffle = playerViewModel::toggleShuffle,
        onPrevious = playerViewModel::skipToPrevious,
        onPlayPause = playerViewModel::playPause,
        onNext = playerViewModel::skipToNext,
        onCycleRepeat = playerViewModel::cycleRepeat,
        modifier = modifier,
        highlightedPlay = highlightedPlay,
    )
}

@Composable
fun NowPlayingIconControls(
    playbackState: PlaybackState,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    highlightedPlay: Boolean = true,
) {
    val tokens = LocalIalemusTokens.current
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HudIconButton(
                icon = Icons.Filled.Shuffle,
                contentDescription = if (playbackState.shuffleEnabled) "Shuffle on" else "Shuffle off",
                onClick = onToggleShuffle,
                highlighted = playbackState.shuffleEnabled,
            )
            HudIconButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                onClick = onPrevious,
                enabled = playbackState.canSkipPrevious,
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = if (highlightedPlay) 1.5.dp else 1.dp,
                        color = if (highlightedPlay) tokens.accentActive else tokens.hudBorderColor,
                        shape = MaterialTheme.shapes.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = tokens.accentActive,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            HudIconButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = "Next",
                onClick = onNext,
                enabled = playbackState.canSkipNext,
            )
            HudIconButton(
                icon = when (playbackState.repeatMode) {
                    RepeatMode.ONE -> Icons.Filled.RepeatOne
                    else -> Icons.Filled.Repeat
                },
                contentDescription = "Repeat ${playbackState.repeatMode.displayName}",
                onClick = onCycleRepeat,
                highlighted = playbackState.repeatMode != RepeatMode.OFF,
            )
        }
        if (playbackState.shuffleEnabled || playbackState.repeatMode != RepeatMode.OFF) {
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (playbackState.shuffleEnabled) {
                    HudStatusChip(label = "SHUFFLE", highlighted = true)
                }
                if (playbackState.repeatMode != RepeatMode.OFF) {
                    HudStatusChip(
                        label = when (playbackState.repeatMode) {
                            RepeatMode.ONE -> "REPEAT ONE"
                            RepeatMode.QUEUE -> "REPEAT QUEUE"
                            RepeatMode.OFF -> "REPEAT"
                        },
                        warning = true,
                    )
                }
            }
        }
    }
}

@Composable
fun NowPlayingActionIconRow(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onToggleLyrics: () -> Unit,
    onToggleMetadata: () -> Unit,
    onToggleCleanup: () -> Unit,
    onToggleTools: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    metadataActive: Boolean = false,
    cleanupActive: Boolean = false,
    toolsActive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudIconButton(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
            onClick = onToggleFavorite,
            highlighted = isFavorite,
        )
        HudIconButton(
            icon = Icons.Filled.PlaylistAdd,
            contentDescription = "Add to playlist",
            onClick = onAddToPlaylist,
        )
        HudIconButton(
            icon = Icons.Filled.QueueMusic,
            contentDescription = "Open queue",
            onClick = onOpenQueue,
        )
        HudIconButton(
            icon = Icons.Filled.Lyrics,
            contentDescription = "Lyrics",
            onClick = onToggleLyrics,
        )
        HudIconButton(
            icon = Icons.Filled.Info,
            contentDescription = "Local signal metadata",
            onClick = onToggleMetadata,
            highlighted = metadataActive,
        )
        HudIconButton(
            icon = Icons.Filled.Edit,
            contentDescription = "Track cleanup",
            onClick = onToggleCleanup,
            highlighted = cleanupActive,
        )
        HudIconButton(
            icon = Icons.Filled.MoreHoriz,
            contentDescription = "Audio tools",
            onClick = onToggleTools,
            highlighted = toolsActive,
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
    NowPlayingActionIconRow(
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onOpenQueue = onOpenQueue,
        onToggleLyrics = {},
        onToggleMetadata = {},
        onToggleCleanup = {},
        onToggleTools = {},
        modifier = modifier,
    )
}

@Composable
fun NowPlayingShuffleRepeatRow(
    playbackState: PlaybackState,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    // Merged into NowPlayingIconControls — kept for layout compatibility.
}

@Composable
fun NowPlayingMetadataPanel(
    track: Track,
    playbackState: PlaybackState,
    isFavorite: Boolean,
    playCount: Int?,
    lastPlayedAt: Long?,
    override: TrackOverrideEntity?,
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
        if (override?.displayTitleOverride != null) {
            MetadataLine("Original title", track.title)
            MetadataLine("Display override", override.displayTitleOverride)
        } else {
            MetadataLine("Scanned title", track.title)
        }
        MetadataLine("Artist", track.displayArtist)
        if (override?.displayArtistOverride != null) {
            MetadataLine("Original artist", track.artist.orEmpty())
            MetadataLine("Artist override", override.displayArtistOverride)
        }
        MetadataLine("Album", track.displayAlbum)
        if (override?.displayAlbumOverride != null) {
            MetadataLine("Original album", track.album.orEmpty())
            MetadataLine("Album override", override.displayAlbumOverride)
        }
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
    override: TrackOverrideEntity?,
    onSaveOverrides: (title: String, artist: String, album: String) -> Unit,
    onResetOverrides: () -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editTitle by remember(track.id, track.displayTitle) { mutableStateOf(track.displayTitle) }
    var editArtist by remember(track.id, track.displayArtist) { mutableStateOf(track.displayArtist) }
    var editAlbum by remember(track.id, track.displayAlbum) { mutableStateOf(track.displayAlbum) }
    val suggested = remember(track.title) { TrackTitleCleanup.stripTrackNumberPrefix(track.title) }
    val hasOverride = override?.displayTitleOverride != null ||
        override?.displayArtistOverride != null ||
        override?.displayAlbumOverride != null

    HudCollapsiblePanel(
        title = "Track Cleanup",
        sectionTag = "DISPLAY OVERRIDE",
        subtitle = "Override display metadata without renaming files.",
        expanded = expanded,
        onToggle = onToggle,
        statusLabel = if (hasOverride) "OVERRIDE" else "EDIT",
        modifier = modifier,
    ) {
        HudOutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            modifier = Modifier.fillMaxWidth(),
            label = "Display title",
        )
        HudOutlinedTextField(
            value = editArtist,
            onValueChange = { editArtist = it },
            modifier = Modifier.fillMaxWidth(),
            label = "Display artist",
        )
        HudOutlinedTextField(
            value = editAlbum,
            onValueChange = { editAlbum = it },
            modifier = Modifier.fillMaxWidth(),
            label = "Display album",
        )
        if (suggested != null && suggested != track.displayTitle) {
            Text(
                text = "Suggested title: \"$suggested\"",
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
                label = "Save overrides",
                onClick = { onSaveOverrides(editTitle, editArtist, editAlbum) },
                modifier = Modifier.weight(1f),
            )
            if (hasOverride) {
                HudButton(
                    label = "Reset all",
                    onClick = onResetOverrides,
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Warning,
                )
            }
        }
        Text(
            text = "Overrides affect display only. Embedded tag editing requires future write support.",
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
    // Legacy alias — use NowPlayingEmptyState from layouts.
    HudPanel(title = "Inactive", sectionTag = "CORE", modifier = modifier) {
        HudButton(label = "Open Library", onClick = onOpenLibrary)
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
