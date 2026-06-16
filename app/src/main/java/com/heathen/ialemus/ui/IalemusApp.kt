package com.heathen.ialemus.ui

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.spotify.SpotifyViewModel
import com.heathen.ialemus.ui.components.HudBottomNavigation
import com.heathen.ialemus.ui.components.HudScaffold
import com.heathen.ialemus.ui.components.MiniPlayerBar
import com.heathen.ialemus.ui.navigation.AppDestination
import com.heathen.ialemus.ui.screens.DownloadsScreen
import com.heathen.ialemus.ui.screens.StreamingScreen
import com.heathen.ialemus.ui.screens.library.LibraryScreen
import com.heathen.ialemus.ui.screens.nowplaying.NowPlayingScreen
import com.heathen.ialemus.ui.screens.settings.SettingsScreen
import com.heathen.ialemus.ui.theme.IalemusTheme
import com.heathen.ialemus.ui.theme.screenHorizontalPadding

@Composable
fun IalemusApp(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    spotifyViewModel: SpotifyViewModel,
    onRequestNotificationPermission: () -> Unit = {},
) {
    var destination by rememberSaveable {
        mutableStateOf(AppDestination.NOW_PLAYING)
    }
    var hideMiniPlayerForWebView by rememberSaveable { mutableStateOf(false) }
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()
    val showMiniPlayerBar by settingsViewModel.showMiniPlayerBar.collectAsStateWithLifecycle()
    val nowPlayingLayoutMode by settingsViewModel.nowPlayingLayoutMode.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val horizontalPad = screenHorizontalPadding()

    val onNowPlayingScreen = destination == AppDestination.NOW_PLAYING
    val shouldShowMiniPlayer = showMiniPlayerBar &&
        !hideMiniPlayerForWebView &&
        !onNowPlayingScreen &&
        playbackState.currentTrack != null

    LaunchedEffect(Unit) {
        libraryViewModel.refreshPermissionState()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onRequestNotificationPermission()
        }
    }

    LaunchedEffect(playbackState.playbackError) {
        val message = playbackState.playbackError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        playerViewModel.clearPlaybackError()
    }

    LaunchedEffect(destination) {
        if (destination != AppDestination.DOWNLOADS) {
            hideMiniPlayerForWebView = false
        }
    }

    IalemusTheme(themeId = selectedTheme, dapMode = dapMode) {
        HudScaffold(
            snackbarHostState = snackbarHostState,
            bottomBar = {
                Column {
                    if (shouldShowMiniPlayer) {
                        MiniPlayerBar(
                            track = playbackState.currentTrack,
                            isPlaying = playbackState.isPlaying,
                            shuffleEnabled = playbackState.shuffleEnabled,
                            repeatMode = playbackState.repeatMode,
                            canSkipPrevious = playbackState.canSkipPrevious,
                            canSkipNext = playbackState.canSkipNext,
                            onPlayPause = playerViewModel::playPause,
                            onPrevious = playerViewModel::skipToPrevious,
                            onNext = playerViewModel::skipToNext,
                            onToggleShuffle = playerViewModel::toggleShuffle,
                            onCycleRepeat = playerViewModel::cycleRepeat,
                            onOpenNowPlaying = { destination = AppDestination.NOW_PLAYING },
                            modifier = Modifier.padding(horizontal = horizontalPad, vertical = 4.dp),
                        )
                    }
                    HudBottomNavigation(
                        selected = destination,
                        onSelect = { destination = it },
                    )
                }
            },
        ) { innerPadding ->
            when (destination) {
                AppDestination.NOW_PLAYING -> NowPlayingScreen(
                    playerViewModel = playerViewModel,
                    layoutMode = nowPlayingLayoutMode,
                    onOpenLibrary = { destination = AppDestination.LIBRARY },
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.LIBRARY -> LibraryScreen(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.STREAMING -> StreamingScreen(
                    spotifyViewModel = spotifyViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.DOWNLOADS -> DownloadsScreen(
                    settingsViewModel = settingsViewModel,
                    onWebViewActive = { hideMiniPlayerForWebView = it },
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.SETTINGS -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    spotifyViewModel = spotifyViewModel,
                    libraryViewModel = libraryViewModel,
                    onOpenSpotifyExperimental = { destination = AppDestination.STREAMING },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
