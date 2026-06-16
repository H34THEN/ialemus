package com.heathen.ialemus.ui

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.MiniPlayerBar
import com.heathen.ialemus.ui.navigation.AppDestination
import com.heathen.ialemus.ui.screens.AcquireScreen
import com.heathen.ialemus.ui.screens.DownloadsScreen
import com.heathen.ialemus.ui.screens.library.LibraryScreen
import com.heathen.ialemus.ui.screens.nowplaying.NowPlayingScreen
import com.heathen.ialemus.ui.screens.settings.SettingsScreen
import com.heathen.ialemus.ui.theme.IalemusTheme

/**
 * Root Compose scaffold with state-based bottom navigation (MVP 1A).
 *
 * TODO: Replace with Navigation Compose NavHost for deep links and back stack.
 * TODO: Landscape layouts are first-class — add adaptive navigation rail / multi-pane (MVP 5).
 */
@Composable
fun IalemusApp(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    onRequestNotificationPermission: () -> Unit = {},
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.NOW_PLAYING) }
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        libraryViewModel.refreshPermissionState()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onRequestNotificationPermission()
        }
    }

    IalemusTheme(themeId = selectedTheme) {
        Scaffold(
            bottomBar = {
                androidx.compose.foundation.layout.Column {
                    MiniPlayerBar(
                        track = playbackState.currentTrack,
                        isPlaying = playbackState.isPlaying,
                        onPlayPause = playerViewModel::playPause,
                        onOpenNowPlaying = { destination = AppDestination.NOW_PLAYING },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    NavigationBar {
                        AppDestination.entries.forEach { item ->
                            NavigationBarItem(
                                selected = destination == item,
                                onClick = { destination = item },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                    )
                                },
                                label = { Text(item.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            when (destination) {
                AppDestination.NOW_PLAYING -> NowPlayingScreen(
                    playerViewModel = playerViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.LIBRARY -> LibraryScreen(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.ACQUIRE -> AcquireScreen(
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.DOWNLOADS -> DownloadsScreen(
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.SETTINGS -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
