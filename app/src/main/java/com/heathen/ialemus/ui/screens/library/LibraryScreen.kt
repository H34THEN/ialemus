package com.heathen.ialemus.ui.screens.library

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.LibraryBrowseMode
import com.heathen.ialemus.core.model.LibraryDetail
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.LibraryBrowseTabRow
import com.heathen.ialemus.ui.components.MusicSourcePanel
import com.heathen.ialemus.ui.components.PermissionGateCard
import com.heathen.ialemus.ui.theme.screenHorizontalPadding

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val permissionState by libraryViewModel.permissionState.collectAsStateWithLifecycle()
    val scanState by libraryViewModel.scanState.collectAsStateWithLifecycle()
    val trackCount by libraryViewModel.trackCount.collectAsStateWithLifecycle()
    val sources by libraryViewModel.librarySources.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    var browseMode by rememberSaveable { mutableStateOf(LibraryBrowseMode.TRACKS) }
    var detailKind by rememberSaveable { mutableStateOf("none") }
    var detailArtist by rememberSaveable { mutableStateOf("") }
    var detailAlbum by rememberSaveable { mutableStateOf("") }
    var detailFolderSourceId by rememberSaveable { mutableStateOf("") }
    var detailFolderPath by rememberSaveable { mutableStateOf("") }
    var detailFolderName by rememberSaveable { mutableStateOf("") }
    var detailPlaylistId by rememberSaveable { mutableStateOf("") }
    var detailPlaylistName by rememberSaveable { mutableStateOf("") }
    var sourceExpanded by rememberSaveable { mutableStateOf(false) }
    var pendingFullDeviceScan by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(trackCount, sources.size) {
        if (trackCount > 0 || sources.isNotEmpty()) {
            sourceExpanded = false
        } else {
            sourceExpanded = true
        }
    }

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
            sourceExpanded = true
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPad, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HudStatusChip(
                label = "LIBRARY · $trackCount",
                highlighted = trackCount > 0,
            )
            HudStatusChip(
                label = if (sourceExpanded) "HIDE SOURCES" else "SOURCES",
                highlighted = sourceExpanded,
                modifier = Modifier.clickable { sourceExpanded = !sourceExpanded },
            )
        }

        if (sourceExpanded) {
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
                expanded = true,
                onToggleExpanded = null,
                defaultExpandedGuide = trackCount == 0,
            )
        }

        if (permissionState != MediaPermissionState.Granted && sourceExpanded) {
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

        if (detailKind != "none") {
            val detail = when (detailKind) {
                "artist" -> LibraryDetail.Artist(detailArtist)
                "album" -> LibraryDetail.Album(detailAlbum, detailArtist)
                "folder" -> LibraryDetail.Folder(detailFolderSourceId, detailFolderPath, detailFolderName)
                "playlist" -> LibraryDetail.Playlist(detailPlaylistId, detailPlaylistName)
                else -> null
            }
            if (detail != null) {
                LibraryDetailContent(
                    detail = detail,
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel,
                    currentTrackId = playbackState.currentTrack?.id,
                    onBack = { detailKind = "none" },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            LibraryBrowseTabRow(
                selected = browseMode,
                onSelect = { browseMode = it },
            )
            LibraryBrowserContent(
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                browseMode = browseMode,
                onOpenDetail = { detail ->
                    when (detail) {
                        is LibraryDetail.Artist -> {
                            detailKind = "artist"
                            detailArtist = detail.artist
                        }
                        is LibraryDetail.Album -> {
                            detailKind = "album"
                            detailAlbum = detail.album
                            detailArtist = detail.artist
                        }
                        is LibraryDetail.Folder -> {
                            detailKind = "folder"
                            detailFolderSourceId = detail.librarySourceId
                            detailFolderPath = detail.folderPath
                            detailFolderName = detail.displayName
                        }
                        is LibraryDetail.Playlist -> {
                            detailKind = "playlist"
                            detailPlaylistId = detail.playlistId
                            detailPlaylistName = detail.name
                        }
                    }
                },
                currentTrackId = playbackState.currentTrack?.id,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
