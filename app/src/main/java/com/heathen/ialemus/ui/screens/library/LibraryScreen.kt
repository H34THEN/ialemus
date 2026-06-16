package com.heathen.ialemus.ui.screens.library

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.EmptyLibraryState
import com.heathen.ialemus.ui.components.PermissionGateCard
import com.heathen.ialemus.ui.components.ScanProgressCard
import com.heathen.ialemus.ui.components.TrackRow

private enum class LibraryTab(val label: String) {
    TRACKS("Tracks"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    FOLDERS("Folders"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionState by libraryViewModel.permissionState.collectAsStateWithLifecycle()
    val scanState by libraryViewModel.scanState.collectAsStateWithLifecycle()
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val trackCount by libraryViewModel.trackCount.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(LibraryTab.TRACKS) }

    LaunchedEffect(Unit) {
        libraryViewModel.refreshPermissionState()
    }

    val activity = context as? androidx.activity.ComponentActivity

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val shouldShow = activity?.shouldShowRequestPermissionRationale(
            libraryViewModel.requiredPermission(),
        ) == true
        libraryViewModel.onPermissionResult(granted, shouldShow)
        if (granted) {
            libraryViewModel.scanLocalLibrary()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Library") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "$trackCount local tracks indexed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            PermissionGateCard(
                permissionState = permissionState,
                onRequestPermission = {
                    permissionLauncher.launch(libraryViewModel.requiredPermission())
                },
                onOpenSettings = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    )
                    context.startActivity(intent)
                },
            )

            if (permissionState == MediaPermissionState.Granted) {
                Button(
                    onClick = { libraryViewModel.scanLocalLibrary() },
                    enabled = scanState !is LibraryScanState.Scanning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Scan local music")
                }
            }

            ScanProgressCard(scanState = scanState)

            LibraryTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )

            when (selectedTab) {
                LibraryTab.TRACKS -> TracksList(
                    tracks = tracks,
                    onTrackClick = { track, index ->
                        playerViewModel.playTracks(tracks, index)
                    },
                    modifier = Modifier.weight(1f),
                )
                LibraryTab.ALBUMS -> EmptyLibraryState(
                    title = "Albums",
                    body = "Album browsing arrives in MVP 1B. Tracks view is live now.",
                )
                LibraryTab.ARTISTS -> EmptyLibraryState(
                    title = "Artists",
                    body = "Artist browsing arrives in MVP 1B. Tracks view is live now.",
                )
                LibraryTab.FOLDERS -> EmptyLibraryState(
                    title = "Folders",
                    body = "Folder browsing arrives in MVP 1B. MediaStore scan is primary for MVP 1A.",
                )
            }

            // TODO: Landscape master-detail library layout (MVP 5).
        }
    }
}

@Composable
private fun LibraryTabRow(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun TracksList(
    tracks: List<Track>,
    onTrackClick: (Track, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tracks.isEmpty()) {
        EmptyLibraryState(
            title = "No local tracks",
            body = "Grant music access and run a scan to index device audio.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                track = track,
                onClick = { onTrackClick(track, index) },
            )
        }
    }
}
