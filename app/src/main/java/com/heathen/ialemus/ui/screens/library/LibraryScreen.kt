package com.heathen.ialemus.ui.screens.library

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudSectionLabel
import com.heathen.ialemus.ui.components.LibraryBrowseTabRow
import com.heathen.ialemus.ui.components.MusicSourcePanel
import com.heathen.ialemus.ui.components.PermissionGateCard

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
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
    var sourceExpanded by rememberSaveable { mutableStateOf(true) }
    var pendingFullDeviceScan by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(trackCount) {
        if (trackCount > 0) {
            sourceExpanded = false
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

    val sourceCallbacks = SourceCallbacks(
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
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HudHeader(
            title = "Library",
            statusLabel = "TRACK INDEX",
            subtitle = "SIGNAL INDEX · $trackCount tracks",
        )

        MusicSourcePanel(
            scanState = scanState,
            sources = sources,
            permissionState = permissionState,
            onChooseFolder = sourceCallbacks.onChooseFolder,
            onScanSelectedFolders = sourceCallbacks.onScanSelectedFolders,
            onFullDeviceScan = sourceCallbacks.onFullDeviceScan,
            onRequestPermission = sourceCallbacks.onRequestPermission,
            onRemoveSource = sourceCallbacks.onRemoveSource,
            isScanning = isScanning,
            expanded = sourceExpanded,
            onToggleExpanded = { sourceExpanded = !sourceExpanded },
            defaultExpandedGuide = trackCount == 0,
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

        if (detailKind != "none") {
            val detail = when (detailKind) {
                "artist" -> LibraryDetail.Artist(detailArtist)
                "album" -> LibraryDetail.Album(detailAlbum, detailArtist)
                "folder" -> LibraryDetail.Folder(detailFolderSourceId, detailFolderPath, detailFolderName)
                else -> null
            }
            if (detail != null) {
                LibraryDetailContent(
                    detail = detail,
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel,
                    currentTrackId = playbackState.currentTrack?.id,
                    onBack = {
                        detailKind = "none"
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            HudSectionLabel(
                label = "Library Browser",
                trailing = browseMode.label.uppercase(),
            )
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
                    }
                },
                currentTrackId = playbackState.currentTrack?.id,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private data class SourceCallbacks(
    val onChooseFolder: () -> Unit,
    val onScanSelectedFolders: () -> Unit,
    val onFullDeviceScan: () -> Unit,
    val onRequestPermission: () -> Unit,
    val onRemoveSource: (String) -> Unit,
)
