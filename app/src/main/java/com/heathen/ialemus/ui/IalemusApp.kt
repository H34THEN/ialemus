package com.heathen.ialemus.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.ui.navigation.AppDestination
import com.heathen.ialemus.ui.screens.AcquireScreen
import com.heathen.ialemus.ui.screens.DownloadsScreen
import com.heathen.ialemus.ui.screens.LibraryScreen
import com.heathen.ialemus.ui.screens.NowPlayingScreen
import com.heathen.ialemus.ui.screens.SettingsScreen
import com.heathen.ialemus.ui.theme.IalemusTheme

/**
 * Root Compose scaffold with state-based bottom navigation (MVP 0).
 *
 * TODO: Replace with Navigation Compose NavHost for deep links and back stack.
 * TODO: Landscape layouts are first-class — add adaptive navigation rail / multi-pane (MVP 5).
 */
@Composable
fun IalemusApp() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.NOW_PLAYING) }
    var selectedTheme by rememberSaveable { mutableStateOf(ThemeId.DEFAULT) }

    IalemusTheme(themeId = selectedTheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
            },
        ) { innerPadding ->
            when (destination) {
                AppDestination.NOW_PLAYING -> NowPlayingScreen(
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.LIBRARY -> LibraryScreen(
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.ACQUIRE -> AcquireScreen(
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.DOWNLOADS -> DownloadsScreen(
                    modifier = Modifier.padding(innerPadding),
                )
                AppDestination.SETTINGS -> SettingsScreen(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
