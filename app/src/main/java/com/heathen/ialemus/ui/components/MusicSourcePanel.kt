package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.LibrarySource
import com.heathen.ialemus.core.model.LibrarySourceType

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
) {
    HudPanel(
        title = "Music Source",
        sectionTag = "SOURCE SELECT",
        subtitle = scanStateMessage(scanState),
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HudButton(
                label = "Choose Music Folder",
                onClick = onChooseFolder,
                enabled = !isScanning,
            )
            HudButton(
                label = "Scan Selected Folders",
                onClick = onScanSelectedFolders,
                enabled = sources.any { it.type == LibrarySourceType.SAF_FOLDER } && !isScanning,
            )
            HudButton(
                label = "Full Device Music Scan",
                onClick = {
                    if (permissionState != MediaPermissionState.Granted) {
                        onRequestPermission()
                    } else {
                        onFullDeviceScan()
                    }
                },
                enabled = !isScanning,
                accent = HudButtonAccent.Warning,
            )
            Text(
                text = "Full device scan uses Android MediaStore and requires broader music access. It never runs automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )

            HudSectionLabel(label = "Manage Sources")

            if (sources.isEmpty()) {
                HudStatusChip(label = "No folders approved", disabled = true)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    sources.filter { it.type == LibrarySourceType.SAF_FOLDER }.forEach { source ->
                        HudStatusChip(label = source.displayName, highlighted = true)
                        HudButton(
                            label = "Remove ${source.displayName}",
                            onClick = { onRemoveSource(source.id) },
                            accent = HudButtonAccent.Danger,
                            modifier = Modifier.fillMaxWidth(0.48f),
                        )
                    }
                }
            }

            ScanStatusPanel(scanState = scanState, isScanning = isScanning)
        }
    }
}

@Composable
private fun ScanStatusPanel(
    scanState: LibraryScanState,
    isScanning: Boolean,
) {
    val statusLabel = when {
        isScanning -> "SCANNING"
        scanState is LibraryScanState.Failed -> "SCAN ERROR"
        scanState is LibraryScanState.FolderScanComplete ||
            scanState is LibraryScanState.FullDeviceComplete -> "INDEX READY"
        else -> "SCAN STANDBY"
    }
    HudPanel(
        title = "Scan Status",
        sectionTag = "QUEUE SYNC",
        subtitle = scanStateMessage(scanState),
    ) {
        HudStatusChip(
            label = statusLabel,
            highlighted = !isScanning && scanState !is LibraryScanState.Failed,
            warning = isScanning,
            disabled = scanState is LibraryScanState.NoSources,
        )
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
