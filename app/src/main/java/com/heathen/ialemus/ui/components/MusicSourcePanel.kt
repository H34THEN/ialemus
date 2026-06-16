package com.heathen.ialemus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.LibrarySource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MusicSourcePanel(
    scanState: LibraryScanState,
    sources: List<LibrarySource>,
    permissionState: MediaPermissionState,
    onChooseFolder: () -> Unit,
    onScanSelectedFolders: () -> Unit,
    onFullDeviceScan: () -> Unit,
    onRequestPermission: () -> Unit,
    onRemoveSource: (String) -> Unit,
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    expanded: Boolean = true,
    onToggleExpanded: (() -> Unit)? = null,
    defaultExpandedGuide: Boolean = false,
) {
    val statusLabel = when {
        isScanning -> "SCANNING"
        scanState is LibraryScanState.FolderScanComplete ||
            scanState is LibraryScanState.FullDeviceComplete -> "READY"
        sources.isEmpty() -> "SETUP"
        else -> "${sources.size} SOURCES"
    }

    if (onToggleExpanded != null) {
        HudCollapsiblePanel(
            title = "Music Source",
            sectionTag = "SOURCE SELECT",
            subtitle = scanStateMessage(scanState),
            expanded = expanded,
            onToggle = onToggleExpanded,
            statusLabel = statusLabel,
            defaultExpandedGuide = defaultExpandedGuide,
            modifier = modifier,
        ) {
            MusicSourceControls(
                scanState = scanState,
                sources = sources,
                permissionState = permissionState,
                onChooseFolder = onChooseFolder,
                onScanSelectedFolders = onScanSelectedFolders,
                onFullDeviceScan = onFullDeviceScan,
                onRequestPermission = onRequestPermission,
                onRemoveSource = onRemoveSource,
                isScanning = isScanning,
            )
        }
    } else {
        HudPanel(
            title = "Music Source",
            sectionTag = "SOURCE SELECT",
            subtitle = scanStateMessage(scanState),
            modifier = modifier,
        ) {
            MusicSourceControls(
                scanState = scanState,
                sources = sources,
                permissionState = permissionState,
                onChooseFolder = onChooseFolder,
                onScanSelectedFolders = onScanSelectedFolders,
                onFullDeviceScan = onFullDeviceScan,
                onRequestPermission = onRequestPermission,
                onRemoveSource = onRemoveSource,
                isScanning = isScanning,
            )
        }
    }
}

private fun scanStateMessage(state: LibraryScanState): String = when (state) {
    LibraryScanState.NoSources -> "No music sources selected. Choose a folder or run a full-device scan explicitly."
    is LibraryScanState.FoldersSelected -> "${state.count} folder(s) selected. Tap Scan Selected Folders."
    LibraryScanState.ScanningFolders -> "Scanning selected folders…"
    is LibraryScanState.FolderScanComplete -> "Folder scan complete: ${state.trackCount} tracks indexed."
    LibraryScanState.FullDeviceAvailable -> "Full-device scan available. Selected-folder scan is preferred."
    LibraryScanState.ScanningFullDevice -> "Running full-device MediaStore scan…"
    is LibraryScanState.FullDeviceComplete -> "Full-device scan complete: ${state.trackCount} tracks indexed."
    is LibraryScanState.Failed -> "Scan failed: ${state.message}"
    LibraryScanState.Idle -> "Ready to scan approved music sources."
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryBrowseTabRow(
    selected: com.heathen.ialemus.core.model.LibraryBrowseMode,
    onSelect: (com.heathen.ialemus.core.model.LibraryBrowseMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        com.heathen.ialemus.core.model.LibraryBrowseMode.entries.forEach { mode ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.clickable { onSelect(mode) },
            ) {
                HudStatusChip(
                    label = mode.label,
                    highlighted = selected == mode,
                )
            }
        }
    }
}
