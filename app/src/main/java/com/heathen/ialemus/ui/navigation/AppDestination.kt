package com.heathen.ialemus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * State-based tab destinations for MVP 0 scaffold.
 *
 * TODO: Migrate to Navigation Compose for deep links, back stack, and landscape master-detail layouts.
 * Landscape layouts are a first-class requirement — see ANDROID_APP_SPEC.md.
 */
enum class AppDestination(
    val label: String,
    val icon: ImageVector,
) {
    NOW_PLAYING("Now Playing", Icons.Filled.PlayCircle),
    LIBRARY("Library", Icons.Filled.LibraryMusic),
    STREAMING("Streaming", Icons.Filled.GraphicEq),
    DOWNLOADS("Downloads", Icons.Filled.CloudDownload),
    SETTINGS("Settings", Icons.Filled.Settings),
}
