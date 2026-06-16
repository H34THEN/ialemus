package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        subtitle = scanStateMessage(scanState),
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onChooseFolder,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isScanning,
            ) {
                Text("Choose Music Folder")
            }
            Button(
                onClick = onScanSelectedFolders,
                modifier = Modifier.fillMaxWidth(),
                enabled = sources.any { it.type == LibrarySourceType.SAF_FOLDER } && !isScanning,
            ) {
                Text("Scan Selected Folders")
            }
            OutlinedButton(
                onClick = {
                    if (permissionState != MediaPermissionState.Granted) {
                        onRequestPermission()
                    } else {
                        onFullDeviceScan()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isScanning,
            ) {
                Text("Full Device Music Scan")
            }
            Text(
                text = "Full device scan uses Android MediaStore and requires broader music access. It never runs automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )

            if (sources.isNotEmpty()) {
                Text(
                    text = "MANAGE FOLDERS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sources.filter { it.type == LibrarySourceType.SAF_FOLDER }.forEach { source ->
                        StatusChip(label = source.displayName)
                        TextButton(onClick = { onRemoveSource(source.id) }) {
                            Text("Remove")
                        }
                    }
                }
            }
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
