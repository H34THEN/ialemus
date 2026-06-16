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
import com.heathen.ialemus.ui.components.HudBottomNavigation
import com.heathen.ialemus.ui.components.HudScaffold
import com.heathen.ialemus.ui.components.MiniPlayerBar
import com.heathen.ialemus.ui.navigation.AppDestination
import com.heathen.ialemus.ui.screens.AcquireScreen
import com.heathen.ialemus.ui.screens.DownloadsScreen
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
    onRequestNotificationPermission: () -> Unit = {},
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.NOW_PLAYING) }
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val horizontalPad = screenHorizontalPadding()

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

    IalemusTheme(themeId = selectedTheme, dapMode = dapMode) {
        HudScaffold(
            snackbarHostState = snackbarHostState,
            bottomBar = {
                Column {
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
                    onOpenLibrary = { destination = AppDestination.LIBRARY },
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.LIBRARY -> LibraryScreen(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.ACQUIRE -> AcquireScreen(
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.DOWNLOADS -> DownloadsScreen(
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.SETTINGS -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    libraryViewModel = libraryViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
