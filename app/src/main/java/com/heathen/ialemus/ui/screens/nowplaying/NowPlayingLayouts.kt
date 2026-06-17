package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.model.NowPlayingVisualizerMode
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.data.local.entity.TrackOverrideEntity
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.isCompactWidth
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.formatDuration

data class NowPlayingEmptyCallbacks(
    val trackCount: Int = 0,
    val sourceCount: Int = 0,
    val scanState: LibraryScanState = LibraryScanState.NoSources,
    val lastPlayedTrack: Track? = null,
    val onChooseFolder: () -> Unit = {},
    val onSyncAll: () -> Unit = {},
    val onSyncFolder: () -> Unit = {},
    val onShuffleAll: () -> Unit = {},
    val onResumeLast: (Track) -> Unit = {},
)

@Composable
fun NowPlayingLayoutRouter(
    layoutMode: NowPlayingLayoutMode,
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    override: TrackOverrideEntity?,
    onSaveOverrides: (String, String, String) -> Unit,
    onResetOverrides: () -> Unit,
    onAddToPlaylist: () -> Unit,
    emptyCallbacks: NowPlayingEmptyCallbacks,
    playCount: Int? = null,
    lastPlayedAt: Long? = null,
    visualizerMode: NowPlayingVisualizerMode = NowPlayingVisualizerMode.SIGNAL_BARS,
    dapMode: Boolean = false,
    onCycleVisualizer: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    when (layoutMode) {
        NowPlayingLayoutMode.BALANCED -> BalancedNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            override, onSaveOverrides, onResetOverrides, onAddToPlaylist, emptyCallbacks,
            playCount, lastPlayedAt, modifier, imageHeavy = false, textFirst = false,
        )
        NowPlayingLayoutMode.IMAGE_HEAVY -> BalancedNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            override, onSaveOverrides, onResetOverrides, onAddToPlaylist, emptyCallbacks,
            playCount, lastPlayedAt, modifier, imageHeavy = true, textFirst = false,
        )
        NowPlayingLayoutMode.TEXT_METADATA -> BalancedNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            override, onSaveOverrides, onResetOverrides, onAddToPlaylist, emptyCallbacks,
            playCount, lastPlayedAt, modifier, imageHeavy = false, textFirst = true,
        )
        NowPlayingLayoutMode.PLAYLIST_RADIO -> PlaylistRadioNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            override, onSaveOverrides, onResetOverrides, onAddToPlaylist, emptyCallbacks,
            playCount, lastPlayedAt, modifier,
        )
        NowPlayingLayoutMode.CYBERPUNK_HUD -> CyberpunkHudNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            override, onSaveOverrides, onResetOverrides, onAddToPlaylist, emptyCallbacks,
            playCount, lastPlayedAt, modifier,
            visualizerMode = visualizerMode,
            dapMode = dapMode,
            onCycleVisualizer = onCycleVisualizer,
        )
    }
}

data class NowPlayingPanelState(
    val metadataExpanded: Boolean = false,
    val queueExpanded: Boolean = false,
    val cleanupExpanded: Boolean = false,
    val lyricsExpanded: Boolean = false,
    val toolsExpanded: Boolean = false,
    val showTechnicalDetails: Boolean = false,
    val playlistQueueExpanded: Boolean = false,
)

@Composable
private fun NowPlayingLayoutScaffold(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    override: TrackOverrideEntity?,
    onSaveOverrides: (String, String, String) -> Unit,
    onResetOverrides: () -> Unit,
    onAddToPlaylist: () -> Unit,
    emptyCallbacks: NowPlayingEmptyCallbacks,
    playCount: Int?,
    lastPlayedAt: Long?,
    modifier: Modifier = Modifier,
    imageHeavy: Boolean = false,
    textFirst: Boolean = false,
    topContent: (@Composable () -> Unit)? = null,
) {
    val track = uiState.track
    val playbackState = uiState.playbackState
    val compact = isCompactWidth()
    val horizontalPad = screenHorizontalPadding()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontalPad)
            .padding(vertical = 8.dp),
    ) {
        NowPlayingStatusRow(track = track, playbackState = playbackState)

        if (track == null) {
            NowPlayingEmptyState(
                trackCount = emptyCallbacks.trackCount,
                sourceCount = emptyCallbacks.sourceCount,
                scanState = emptyCallbacks.scanState,
                lastPlayedTrack = emptyCallbacks.lastPlayedTrack,
                onChooseFolder = emptyCallbacks.onChooseFolder,
                onSyncAll = emptyCallbacks.onSyncAll,
                onSyncFolder = emptyCallbacks.onSyncFolder,
                onOpenLibrary = onOpenLibrary,
                onShuffleAll = emptyCallbacks.onShuffleAll,
                onResumeLast = emptyCallbacks.onResumeLast,
                modifier = Modifier.fillMaxWidth(),
            )
            return@Column
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (topContent != null) {
                topContent()
            } else if (textFirst) {
                NowPlayingTextMetadataHeader(track = track)
            } else {
                NowPlayingArtworkPanel(track = track, compact = compact, imageHeavy = imageHeavy)
                NowPlayingTrackHeader(track = track, centered = true, compact = imageHeavy)
            }

            NowPlayingSeekBar(playbackState = playbackState, onSeek = playerViewModel::seekTo)
            NowPlayingPrimaryControls(
                playbackState = playbackState,
                onToggleShuffle = playerViewModel::toggleShuffle,
                onPrevious = playerViewModel::skipToPrevious,
                onPlayPause = playerViewModel::playPause,
                onNext = playerViewModel::skipToNext,
                onCycleRepeat = playerViewModel::cycleRepeat,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            NowPlayingActionIconRow(
                isFavorite = uiState.isFavorite,
                onToggleFavorite = {
                    playerViewModel.toggleFavorite(track.id, !uiState.isFavorite)
                },
                onOpenQueue = onOpenQueue,
                onAddToPlaylist = onAddToPlaylist,
                onToggleLyrics = {
                    onPanelStateChange(panelState.copy(lyricsExpanded = !panelState.lyricsExpanded))
                },
                onToggleMetadata = {
                    onPanelStateChange(panelState.copy(metadataExpanded = !panelState.metadataExpanded))
                },
                onToggleCleanup = {
                    onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
                },
                onToggleTools = {
                    onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
                },
                metadataActive = panelState.metadataExpanded,
                cleanupActive = panelState.cleanupExpanded,
                toolsActive = panelState.toolsExpanded,
            )

            NowPlayingMetadataPanel(
                track = track,
                playbackState = playbackState,
                isFavorite = uiState.isFavorite,
                playCount = playCount,
                lastPlayedAt = lastPlayedAt,
                override = override,
                expanded = panelState.metadataExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(metadataExpanded = !panelState.metadataExpanded))
                },
                showTechnicalDetails = panelState.showTechnicalDetails,
                onToggleTechnicalDetails = {
                    onPanelStateChange(panelState.copy(showTechnicalDetails = !panelState.showTechnicalDetails))
                },
            )

            NowPlayingQueuePreview(
                queueItems = uiState.queueItems,
                onPlayQueueItem = playerViewModel::playQueueItem,
                expanded = panelState.queueExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(queueExpanded = !panelState.queueExpanded))
                },
            )

            HudCollapsiblePanel(
                title = "Lyrics",
                sectionTag = "LYRICS",
                subtitle = "Local lyrics panel — coming soon.",
                expanded = panelState.lyricsExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(lyricsExpanded = !panelState.lyricsExpanded))
                },
                statusLabel = "TODO",
            ) {
                Text(
                    text = "Lyrics placeholder for MVP 1B.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalIalemusTokens.current.textMuted,
                )
            }

            NowPlayingTrackCleanupPanel(
                track = track,
                override = override,
                onSaveOverrides = onSaveOverrides,
                onResetOverrides = onResetOverrides,
                expanded = panelState.cleanupExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
                },
            )

            NowPlayingAudioToolsPanel(
                track = track,
                playbackState = playbackState,
                expanded = panelState.toolsExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
                },
                onSetSpeed = playerViewModel::setPlaybackSpeed,
                onSetSleepTimer = playerViewModel::setSleepTimer,
            )
        }
    }
}

@Composable
private fun BalancedNowPlayingLayout(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    override: TrackOverrideEntity?,
    onSaveOverrides: (String, String, String) -> Unit,
    onResetOverrides: () -> Unit,
    onAddToPlaylist: () -> Unit,
    emptyCallbacks: NowPlayingEmptyCallbacks,
    playCount: Int?,
    lastPlayedAt: Long?,
    modifier: Modifier = Modifier,
    imageHeavy: Boolean,
    textFirst: Boolean,
) {
    val effectivePanelState = if (textFirst) panelState.copy(metadataExpanded = true) else panelState
    NowPlayingLayoutScaffold(
        uiState, playerViewModel, onOpenLibrary, onOpenQueue, effectivePanelState, onPanelStateChange,
        override, onSaveOverrides, onResetOverrides, onAddToPlaylist, emptyCallbacks,
        playCount, lastPlayedAt, modifier, imageHeavy = imageHeavy, textFirst = textFirst,
    )
}

@Composable
private fun PlaylistRadioNowPlayingLayout(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    override: TrackOverrideEntity?,
    onSaveOverrides: (String, String, String) -> Unit,
    onResetOverrides: () -> Unit,
    onAddToPlaylist: () -> Unit,
    emptyCallbacks: NowPlayingEmptyCallbacks,
    playCount: Int?,
    lastPlayedAt: Long?,
    modifier: Modifier = Modifier,
) {
    val track = uiState.track
    val playbackState = uiState.playbackState
    val horizontalPad = screenHorizontalPadding()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxSize().padding(horizontalPad).padding(vertical = 8.dp),
    ) {
        NowPlayingStatusRow(track = track, playbackState = playbackState)
        if (track == null) {
            NowPlayingEmptyState(
                trackCount = emptyCallbacks.trackCount,
                sourceCount = emptyCallbacks.sourceCount,
                scanState = emptyCallbacks.scanState,
                lastPlayedTrack = emptyCallbacks.lastPlayedTrack,
                onChooseFolder = emptyCallbacks.onChooseFolder,
                onSyncAll = emptyCallbacks.onSyncAll,
                onSyncFolder = emptyCallbacks.onSyncFolder,
                onOpenLibrary = onOpenLibrary,
                onShuffleAll = emptyCallbacks.onShuffleAll,
                onResumeLast = emptyCallbacks.onResumeLast,
            )
            return@Column
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NowPlayingTrackHeader(track = track, centered = false, compact = true)
            NowPlayingSeekBar(playbackState = playbackState, onSeek = playerViewModel::seekTo)
            NowPlayingPrimaryControls(
                playbackState = playbackState,
                onToggleShuffle = playerViewModel::toggleShuffle,
                onPrevious = playerViewModel::skipToPrevious,
                onPlayPause = playerViewModel::playPause,
                onNext = playerViewModel::skipToNext,
                onCycleRepeat = playerViewModel::cycleRepeat,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HudStatusChip(label = "PLAYLIST", highlighted = true)
                HudStatusChip(label = "QUEUE ${playbackState.queueIndex + 1}/${playbackState.queueSize}", highlighted = true)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(scrollState).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NowPlayingActionIconRow(
                isFavorite = uiState.isFavorite,
                onToggleFavorite = { playerViewModel.toggleFavorite(track.id, !uiState.isFavorite) },
                onOpenQueue = onOpenQueue,
                onAddToPlaylist = onAddToPlaylist,
                onToggleLyrics = {},
                onToggleMetadata = {
                    onPanelStateChange(panelState.copy(metadataExpanded = !panelState.metadataExpanded))
                },
                onToggleCleanup = {
                    onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
                },
                onToggleTools = {
                    onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
                },
                toolsActive = panelState.toolsExpanded,
            )
            NowPlayingAudioToolsPanel(
                track = track,
                playbackState = playbackState,
                expanded = panelState.toolsExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
                },
                onSetSpeed = playerViewModel::setPlaybackSpeed,
                onSetSleepTimer = playerViewModel::setSleepTimer,
            )
            NowPlayingMetadataPanel(
                track = track,
                playbackState = playbackState,
                isFavorite = uiState.isFavorite,
                playCount = playCount,
                lastPlayedAt = lastPlayedAt,
                override = override,
                expanded = panelState.metadataExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(metadataExpanded = !panelState.metadataExpanded))
                },
                showTechnicalDetails = panelState.showTechnicalDetails,
                onToggleTechnicalDetails = {
                    onPanelStateChange(panelState.copy(showTechnicalDetails = !panelState.showTechnicalDetails))
                },
            )
            NowPlayingQueuePreview(
                queueItems = uiState.queueItems,
                onPlayQueueItem = playerViewModel::playQueueItem,
                expanded = panelState.playlistQueueExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(playlistQueueExpanded = !panelState.playlistQueueExpanded))
                },
                collapsedPreviewCount = 4,
            )
            Text(
                text = "Radio mode: TODO · smart queue generation",
                style = MaterialTheme.typography.labelSmall,
                color = LocalIalemusTokens.current.textMuted,
            )
            NowPlayingTrackCleanupPanel(
                track = track,
                override = override,
                onSaveOverrides = onSaveOverrides,
                onResetOverrides = onResetOverrides,
                expanded = panelState.cleanupExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
                },
            )
        }
    }
}

@Composable
private fun CyberpunkHudNowPlayingLayout(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    override: TrackOverrideEntity?,
    onSaveOverrides: (String, String, String) -> Unit,
    onResetOverrides: () -> Unit,
    onAddToPlaylist: () -> Unit,
    emptyCallbacks: NowPlayingEmptyCallbacks,
    playCount: Int?,
    lastPlayedAt: Long?,
    modifier: Modifier = Modifier,
    visualizerMode: NowPlayingVisualizerMode,
    dapMode: Boolean,
    onCycleVisualizer: (() -> Unit)?,
) {
    val track = uiState.track
    val playbackState = uiState.playbackState
    val tokens = LocalIalemusTokens.current
    val horizontalPad = screenHorizontalPadding()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontalPad)
            .padding(vertical = 8.dp),
    ) {
        NowPlayingStatusRow(track = track, playbackState = playbackState)
        if (track == null) {
            NowPlayingEmptyState(
                trackCount = emptyCallbacks.trackCount,
                sourceCount = emptyCallbacks.sourceCount,
                scanState = emptyCallbacks.scanState,
                lastPlayedTrack = emptyCallbacks.lastPlayedTrack,
                onChooseFolder = emptyCallbacks.onChooseFolder,
                onSyncAll = emptyCallbacks.onSyncAll,
                onSyncFolder = emptyCallbacks.onSyncFolder,
                onOpenLibrary = onOpenLibrary,
                onShuffleAll = emptyCallbacks.onShuffleAll,
                onResumeLast = emptyCallbacks.onResumeLast,
            )
            return@Column
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NowPlayingCompactArtwork(track = track, sizeDp = 56.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.displayTitle, style = MaterialTheme.typography.titleSmall, color = tokens.accentActive, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(track.displayArtist, style = MaterialTheme.typography.bodySmall, color = tokens.glowColor, maxLines = 1)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HudStatusChip(label = "AUDIO LINK", highlighted = playbackState.isPlaying)
                HudStatusChip(label = "QUEUE SYNC", highlighted = playbackState.queueSize > 0)
                HudStatusChip(label = playbackState.repeatMode.displayName.uppercase(), warning = true)
            }
            NowPlayingSeekBar(playbackState = playbackState, onSeek = playerViewModel::seekTo)
            NowPlayingPrimaryControls(
                playbackState = playbackState,
                onToggleShuffle = playerViewModel::toggleShuffle,
                onPrevious = playerViewModel::skipToPrevious,
                onPlayPause = playerViewModel::playPause,
                onNext = playerViewModel::skipToNext,
                onCycleRepeat = playerViewModel::cycleRepeat,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CyberpunkVisualizerPanel(
                mode = visualizerMode,
                playbackState = playbackState,
                dapMode = dapMode,
                onCycleMode = onCycleVisualizer,
            )

            HudPanel(title = "Playlist Readout", sectionTag = "PLAYLIST") {
                Text(
                    text = "▶ ${track.displayTitle} — ${track.displayArtist}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textPrimary,
                )
                Text(
                    text = "QUEUE ${playbackState.queueIndex + 1}/${playbackState.queueSize.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted,
                )
            }

            NowPlayingActionIconRow(
                isFavorite = uiState.isFavorite,
                onToggleFavorite = { playerViewModel.toggleFavorite(track.id, !uiState.isFavorite) },
                onOpenQueue = onOpenQueue,
                onAddToPlaylist = onAddToPlaylist,
                onToggleLyrics = {
                    onPanelStateChange(panelState.copy(lyricsExpanded = !panelState.lyricsExpanded))
                },
                onToggleMetadata = {
                    onPanelStateChange(panelState.copy(metadataExpanded = !panelState.metadataExpanded))
                },
                onToggleCleanup = {
                    onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
                },
                onToggleTools = {
                    onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
                },
                toolsActive = panelState.toolsExpanded,
            )

            NowPlayingAudioToolsPanel(
                track = track,
                playbackState = playbackState,
                expanded = panelState.toolsExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
                },
                onSetSpeed = playerViewModel::setPlaybackSpeed,
                onSetSleepTimer = playerViewModel::setSleepTimer,
            )

            NowPlayingMetadataPanel(
                track = track,
                playbackState = playbackState,
                isFavorite = uiState.isFavorite,
                playCount = playCount,
                lastPlayedAt = lastPlayedAt,
                override = override,
                expanded = panelState.metadataExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(metadataExpanded = !panelState.metadataExpanded))
                },
                showTechnicalDetails = panelState.showTechnicalDetails,
                onToggleTechnicalDetails = {
                    onPanelStateChange(panelState.copy(showTechnicalDetails = !panelState.showTechnicalDetails))
                },
            )

            NowPlayingQueuePreview(
                queueItems = uiState.queueItems,
                onPlayQueueItem = playerViewModel::playQueueItem,
                expanded = panelState.queueExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(queueExpanded = !panelState.queueExpanded))
                },
            )

            NowPlayingTrackCleanupPanel(
                track = track,
                override = override,
                onSaveOverrides = onSaveOverrides,
                onResetOverrides = onResetOverrides,
                expanded = panelState.cleanupExpanded,
                onToggle = {
                    onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
                },
            )
        }
    }
}
