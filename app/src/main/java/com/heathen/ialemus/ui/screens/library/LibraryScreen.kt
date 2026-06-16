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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.EmptyLibraryState
import com.heathen.ialemus.ui.components.MusicSourcePanel
import com.heathen.ialemus.ui.components.PermissionGateCard
import com.heathen.ialemus.ui.components.StatusChip
import com.heathen.ialemus.ui.components.TrackRow
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

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
    val tokens = LocalIalemusTokens.current
    val permissionState by libraryViewModel.permissionState.collectAsStateWithLifecycle()
    val scanState by libraryViewModel.scanState.collectAsStateWithLifecycle()
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val trackCount by libraryViewModel.trackCount.collectAsStateWithLifecycle()
    val sources by libraryViewModel.librarySources.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(LibraryTab.TRACKS) }
    var pendingFullDeviceScan by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        libraryViewModel.refreshPermissionState()
    }

    val activity = context as? androidx.activity.ComponentActivity

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            val displayName = DocumentFile.fromTreeUri(context, it)?.name ?: "Music Folder"
            libraryViewModel.addMusicFolder(it, displayName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val shouldShow = activity?.shouldShowRequestPermissionRationale(
            libraryViewModel.requiredPermission(),
        ) == true
        libraryViewModel.onPermissionResult(granted, shouldShow)
        if (granted && pendingFullDeviceScan) {
            pendingFullDeviceScan = false
            libraryViewModel.scanFullDeviceLibrary()
        }
    }

    val isScanning = scanState is LibraryScanState.ScanningFolders ||
        scanState is LibraryScanState.ScanningFullDevice

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Library", fontWeight = FontWeight.Bold)
                        Text(
                            text = "SIGNAL INDEX · $trackCount tracks",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.glowColor,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MusicSourcePanel(
                scanState = scanState,
                sources = sources,
                permissionState = permissionState,
                onChooseFolder = { folderLauncher.launch(null) },
                onScanSelectedFolders = libraryViewModel::scanSelectedFolders,
                onFullDeviceScan = {
                    if (permissionState == MediaPermissionState.Granted) {
                        libraryViewModel.scanFullDeviceLibrary()
                    } else {
                        pendingFullDeviceScan = true
                        permissionLauncher.launch(libraryViewModel.requiredPermission())
                    }
                },
                onRequestPermission = {
                    pendingFullDeviceScan = true
                    permissionLauncher.launch(libraryViewModel.requiredPermission())
                },
                onRemoveSource = libraryViewModel::removeMusicFolder,
                isScanning = isScanning,
            )

            if (permissionState != MediaPermissionState.Granted) {
                PermissionGateCard(
                    permissionState = permissionState,
                    onRequestPermission = {
                        pendingFullDeviceScan = false
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
            }

            LibraryTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            when (selectedTab) {
                LibraryTab.TRACKS -> TracksList(
                    tracks = tracks,
                    currentTrackId = playbackState.currentTrack?.id,
                    onTrackClick = { track -> playerViewModel.playTrack(tracks, track) },
                    modifier = Modifier.weight(1f),
                )
                LibraryTab.ALBUMS -> EmptyLibraryState(
                    title = "Albums",
                    body = "Album browsing arrives in MVP 1B.",
                    modifier = Modifier.weight(1f),
                )
                LibraryTab.ARTISTS -> EmptyLibraryState(
                    title = "Artists",
                    body = "Artist browsing arrives in MVP 1B.",
                    modifier = Modifier.weight(1f),
                )
                LibraryTab.FOLDERS -> EmptyLibraryState(
                    title = "Folders",
                    body = "Use Choose Music Folder above to approve SAF sources.",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LibraryTabRow(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    currentTrackId: String?,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tracks.isEmpty()) {
        EmptyLibraryState(
            title = "No local tracks",
            body = "Choose a music folder or run an explicit scan. Full-device scan is opt-in only.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tracks, key = { track -> track.id }) { track ->
            TrackRow(
                track = track,
                isPlaying = track.id == currentTrackId,
                onClick = { onTrackClick(track) },
            )
        }
    }
}
