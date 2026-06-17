package com.heathen.ialemus.ui.screens.nowplaying

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.screens.queue.QueueSheet

@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    settingsViewModel: SettingsViewModel,
    layoutMode: NowPlayingLayoutMode,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val queueItems by playerViewModel.queueItems.collectAsStateWithLifecycle()
    val track = playbackState.currentTrack
    var showQueue by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }
    var panelState by remember { mutableStateOf(NowPlayingPanelState()) }

    val trackCount by libraryViewModel.trackCount.collectAsStateWithLifecycle()
    val sources by libraryViewModel.librarySources.collectAsStateWithLifecycle()
    val scanState by libraryViewModel.scanState.collectAsStateWithLifecycle()
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val recentlyPlayed by libraryViewModel.recentlyPlayedTracks.collectAsStateWithLifecycle()
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    val visualizerMode by settingsViewModel.nowPlayingVisualizerMode.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()

    val isFavorite by playerViewModel
        .observeFavorite(track?.id.orEmpty())
        .collectAsStateWithLifecycle(initialValue = false)

    val override by playerViewModel
        .observeTrackOverride(track?.id.orEmpty())
        .collectAsStateWithLifecycle(initialValue = null)

    val stats by playerViewModel
        .observeTrackStats(track?.id.orEmpty())
        .collectAsStateWithLifecycle(initialValue = null)

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            val displayName = DocumentFile.fromTreeUri(context, it)?.name ?: "Music Folder"
            libraryViewModel.addMusicFolder(it, displayName)
        }
    }

    val uiState = NowPlayingUiState(
        track = track,
        playbackState = playbackState,
        isFavorite = isFavorite,
        queueItems = queueItems,
    )

    val emptyCallbacks = NowPlayingEmptyCallbacks(
        trackCount = trackCount,
        sourceCount = sources.size,
        scanState = scanState,
        lastPlayedTrack = recentlyPlayed.firstOrNull(),
        onChooseFolder = { folderLauncher.launch(null) },
        onSyncAll = libraryViewModel::scanSelectedFolders,
        onSyncFolder = libraryViewModel::scanPrimaryFolder,
        onShuffleAll = {
            if (tracks.isNotEmpty()) playerViewModel.playCollection(tracks, shuffle = true)
        },
        onResumeLast = { lastTrack ->
            playerViewModel.playTrack(tracks.ifEmpty { listOf(lastTrack) }, lastTrack)
        },
    )

    NowPlayingLayoutRouter(
        layoutMode = layoutMode,
        uiState = uiState,
        playerViewModel = playerViewModel,
        onOpenLibrary = onOpenLibrary,
        onOpenQueue = { showQueue = true },
        panelState = panelState,
        onPanelStateChange = { panelState = it },
        override = override,
        onSaveOverrides = { title, artist, album ->
            track?.id?.let { playerViewModel.saveDisplayOverrides(it, title, artist, album) }
        },
        onResetOverrides = {
            track?.id?.let { playerViewModel.clearAllOverrides(it) }
        },
        onAddToPlaylist = { showAddToPlaylist = true },
        emptyCallbacks = emptyCallbacks,
        playCount = stats?.playCount,
        lastPlayedAt = stats?.lastPlayedAt,
        visualizerMode = visualizerMode,
        dapMode = dapMode,
        onCycleVisualizer = { settingsViewModel.cycleNowPlayingVisualizerMode(visualizerMode) },
        modifier = modifier,
    )

    if (showQueue) {
        QueueSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showQueue = false },
        )
    }

    if (showAddToPlaylist && track != null) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylist = false },
            onCreateAndAdd = { name ->
                libraryViewModel.createPlaylistAndAddTrack(name, track.id)
                showAddToPlaylist = false
            },
            onAddToExisting = { playlistId ->
                libraryViewModel.addTrackToPlaylist(playlistId, track.id)
                showAddToPlaylist = false
            },
        )
    }
}
