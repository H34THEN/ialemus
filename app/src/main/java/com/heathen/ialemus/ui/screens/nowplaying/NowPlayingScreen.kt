package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.screens.queue.QueueSheet

@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    layoutMode: NowPlayingLayoutMode,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val queueItems by playerViewModel.queueItems.collectAsStateWithLifecycle()
    val track = playbackState.currentTrack
    var showQueue by remember { mutableStateOf(false) }
    var panelState by remember { mutableStateOf(NowPlayingPanelState()) }

    val isFavorite by playerViewModel
        .observeFavorite(track?.id.orEmpty())
        .collectAsStateWithLifecycle(initialValue = false)

    val override by playerViewModel
        .observeTrackOverride(track?.id.orEmpty())
        .collectAsStateWithLifecycle(initialValue = null)

    val uiState = NowPlayingUiState(
        track = track,
        playbackState = playbackState,
        isFavorite = isFavorite,
        queueItems = queueItems,
    )

    NowPlayingLayoutRouter(
        layoutMode = layoutMode,
        uiState = uiState,
        playerViewModel = playerViewModel,
        onOpenLibrary = onOpenLibrary,
        onOpenQueue = { showQueue = true },
        panelState = panelState,
        onPanelStateChange = { panelState = it },
        hasTitleOverride = override?.displayTitleOverride != null,
        onSaveTitleOverride = { title ->
            track?.id?.let { playerViewModel.saveDisplayTitleOverride(it, title) }
        },
        onResetTitleOverride = {
            track?.id?.let { playerViewModel.clearDisplayTitleOverride(it) }
        },
        modifier = modifier,
    )

    if (showQueue) {
        QueueSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showQueue = false },
        )
    }
}
