package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.isCompactWidth
import com.heathen.ialemus.ui.theme.screenHorizontalPadding

@Composable
fun NowPlayingLayoutRouter(
    layoutMode: NowPlayingLayoutMode,
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (layoutMode) {
        NowPlayingLayoutMode.BALANCED -> BalancedNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        )
        NowPlayingLayoutMode.IMAGE_HEAVY -> ImageHeavyNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        )
        NowPlayingLayoutMode.TEXT_METADATA -> TextMetadataNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        )
        NowPlayingLayoutMode.PLAYLIST_RADIO -> PlaylistRadioNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        )
        NowPlayingLayoutMode.CYBERPUNK_HUD -> CyberpunkHudNowPlayingLayout(
            uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
            hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
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
)

@Composable
private fun NowPlayingLayoutScaffold(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
    imageHeavy: Boolean = false,
    showArtFirst: Boolean = true,
    compactHeader: Boolean = false,
    cyberpunk: Boolean = false,
) {
    val track = uiState.track
    val playbackState = uiState.playbackState
    val compact = isCompactWidth()
    val horizontalPad = screenHorizontalPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontalPad)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NowPlayingStatusRow(track = track, playbackState = playbackState)

        if (track == null) {
            NowPlayingInactivePanel(onOpenLibrary = onOpenLibrary)
            return@Column
        }

        if (cyberpunk) {
            HudPanel(title = "Audio Core", sectionTag = "PLAYBACK LINK") {
                RowHudChips(playbackState)
            }
        }

        if (showArtFirst) {
            NowPlayingArtworkPanel(track = track, compact = compact, imageHeavy = imageHeavy)
        }

        NowPlayingTrackHeader(track = track, centered = !cyberpunk, compact = compactHeader)

        if (!showArtFirst) {
            NowPlayingArtworkPanel(
                track = track,
                compact = true,
                imageHeavy = false,
                modifier = Modifier.fillMaxWidth(0.45f),
            )
        }

        NowPlayingSeekBar(playbackState = playbackState, onSeek = playerViewModel::seekTo)
        NowPlayingIconControls(
            playbackState = playbackState,
            onToggleShuffle = playerViewModel::toggleShuffle,
            onPrevious = playerViewModel::skipToPrevious,
            onPlayPause = playerViewModel::playPause,
            onNext = playerViewModel::skipToNext,
            onCycleRepeat = playerViewModel::cycleRepeat,
        )
        NowPlayingActionIconRow(
            isFavorite = uiState.isFavorite,
            onToggleFavorite = {
                playerViewModel.toggleFavorite(track.id, !uiState.isFavorite)
            },
            onOpenQueue = onOpenQueue,
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
        )

        NowPlayingMetadataPanel(
            track = track,
            playbackState = playbackState,
            isFavorite = uiState.isFavorite,
            playCount = null,
            lastPlayedAt = null,
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
            hasOverride = hasTitleOverride,
            onSaveOverride = onSaveTitleOverride,
            onResetOverride = onResetTitleOverride,
            expanded = panelState.cleanupExpanded,
            onToggle = {
                onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
            },
        )

        HudCollapsiblePanel(
            title = "Audio Tools",
            sectionTag = "SIGNAL",
            subtitle = "Output device, bitrate, and rename tools.",
            expanded = panelState.toolsExpanded,
            onToggle = {
                onPanelStateChange(panelState.copy(toolsExpanded = !panelState.toolsExpanded))
            },
            statusLabel = "TOOLS",
        ) {
            Text(
                text = "Signal/output info TODO. Use Track Cleanup for display title overrides.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalIalemusTokens.current.textMuted,
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
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NowPlayingLayoutScaffold(
        uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
        hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        imageHeavy = false,
        showArtFirst = true,
    )
}

@Composable
private fun ImageHeavyNowPlayingLayout(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NowPlayingLayoutScaffold(
        uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
        hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        imageHeavy = true,
        showArtFirst = true,
        compactHeader = true,
    )
}

@Composable
private fun TextMetadataNowPlayingLayout(
    uiState: NowPlayingUiState,
    playerViewModel: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
    panelState: NowPlayingPanelState,
    onPanelStateChange: (NowPlayingPanelState) -> Unit,
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedMetadata = panelState.copy(metadataExpanded = true)
    NowPlayingLayoutScaffold(
        uiState, playerViewModel, onOpenLibrary, onOpenQueue, expandedMetadata, onPanelStateChange,
        hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        imageHeavy = false,
        showArtFirst = false,
        compactHeader = false,
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
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = uiState.track
    val playbackState = uiState.playbackState
    val compact = isCompactWidth()
    val horizontalPad = screenHorizontalPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontalPad)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NowPlayingStatusRow(track = track, playbackState = playbackState)
        if (track == null) {
            NowPlayingInactivePanel(onOpenLibrary = onOpenLibrary)
            return@Column
        }

        NowPlayingTrackHeader(track = track, centered = false, compact = true)
        NowPlayingIconControls(
            playbackState = playbackState,
            onToggleShuffle = playerViewModel::toggleShuffle,
            onPrevious = playerViewModel::skipToPrevious,
            onPlayPause = playerViewModel::playPause,
            onNext = playerViewModel::skipToNext,
            onCycleRepeat = playerViewModel::cycleRepeat,
        )
        Text(
            text = "Radio mode: TODO · Queue ${playbackState.queueIndex + 1}/${playbackState.queueSize}",
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.textMuted,
        )

        NowPlayingQueuePreview(
            queueItems = uiState.queueItems,
            onPlayQueueItem = playerViewModel::playQueueItem,
            expanded = true,
            onToggle = {
                onPanelStateChange(panelState.copy(queueExpanded = !panelState.queueExpanded))
            },
        )

        NowPlayingArtworkPanel(track = track, compact = compact, imageHeavy = false, modifier = Modifier.fillMaxWidth(0.5f))
        NowPlayingTrackCleanupPanel(
            track = track,
            hasOverride = hasTitleOverride,
            onSaveOverride = onSaveTitleOverride,
            onResetOverride = onResetTitleOverride,
            expanded = panelState.cleanupExpanded,
            onToggle = {
                onPanelStateChange(panelState.copy(cleanupExpanded = !panelState.cleanupExpanded))
            },
        )
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
    hasTitleOverride: Boolean,
    onSaveTitleOverride: (String) -> Unit,
    onResetTitleOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NowPlayingLayoutScaffold(
        uiState, playerViewModel, onOpenLibrary, onOpenQueue, panelState, onPanelStateChange,
        hasTitleOverride, onSaveTitleOverride, onResetTitleOverride, modifier,
        imageHeavy = false,
        showArtFirst = true,
        cyberpunk = true,
    )
}

@Composable
private fun RowHudChips(playbackState: com.heathen.ialemus.core.player.PlaybackState) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        HudStatusChip(label = "AUDIO CORE", highlighted = true)
        HudStatusChip(label = "TRACK INDEX", highlighted = playbackState.currentTrack != null)
        HudStatusChip(label = "QUEUE SYNC", highlighted = playbackState.queueSize > 0)
        HudStatusChip(
            label = playbackState.repeatMode.displayName.uppercase(),
            warning = true,
        )
    }
}
