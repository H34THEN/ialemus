package com.heathen.ialemus.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.model.ConnectionMode
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.settings.NasConnectionSettings
import com.heathen.ialemus.core.settings.NasUrlPlaceholders
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.MusicSourceControls
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()
    val trackCount by settingsViewModel.trackCount.collectAsStateWithLifecycle()
    val nasSettings by settingsViewModel.nasConnectionSettings.collectAsStateWithLifecycle()
    val bridgeStatus by settingsViewModel.bridgeTestStatus.collectAsStateWithLifecycle()
    val meTubeStatus by settingsViewModel.meTubeTestStatus.collectAsStateWithLifecycle()
    val slskdStatus by settingsViewModel.slskdTestStatus.collectAsStateWithLifecycle()
    val permissionState by libraryViewModel.permissionState.collectAsStateWithLifecycle()
    val scanState by libraryViewModel.scanState.collectAsStateWithLifecycle()
    val sources by libraryViewModel.librarySources.collectAsStateWithLifecycle()
    var themeExpanded by rememberSaveable { mutableStateOf(false) }
    var sourceExpanded by rememberSaveable { mutableStateOf(false) }
    var nasExpanded by rememberSaveable { mutableStateOf(false) }
    var libraryExpanded by rememberSaveable { mutableStateOf(true) }
    var aboutExpanded by rememberSaveable { mutableStateOf(false) }
    var pendingFullDeviceScan by rememberSaveable { mutableStateOf(false) }

    var nasDisplayName by rememberSaveable(nasSettings.nasDisplayName) { mutableStateOf(nasSettings.nasDisplayName) }
    var bridgeUrl by rememberSaveable(nasSettings.bridgeUrl) { mutableStateOf(nasSettings.bridgeUrl) }
    var bridgeToken by rememberSaveable(nasSettings.bridgeToken) { mutableStateOf(nasSettings.bridgeToken) }
    var meTubeUrl by rememberSaveable(nasSettings.meTubeUrl) { mutableStateOf(nasSettings.meTubeUrl) }
    var slskdUrl by rememberSaveable(nasSettings.slskdUrl) { mutableStateOf(nasSettings.slskdUrl) }
    var jellyfinUrl by rememberSaveable(nasSettings.jellyfinUrl) { mutableStateOf(nasSettings.jellyfinUrl) }
    var connectionMode by rememberSaveable(nasSettings.connectionMode.name) {
        mutableStateOf(nasSettings.connectionMode)
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HudHeader(
            title = "Settings",
            statusLabel = "DAP MODE",
            subtitle = "Ialemus MVP 1B.1 · NAS connectors + dock polish",
        )

        HudCollapsiblePanel(
            title = "Local Library",
            sectionTag = "LOCAL SIGNAL",
            subtitle = "$trackCount tracks indexed on device.",
            expanded = libraryExpanded,
            onToggle = { libraryExpanded = !libraryExpanded },
            statusLabel = "$trackCount TRACKS",
        ) {
            HudStatusChip(label = "$trackCount tracks", highlighted = trackCount > 0)
            RowSwitch(
                label = "DAP low-power HUD (reduced grid/scanlines)",
                checked = dapMode,
                onCheckedChange = settingsViewModel::setDapMode,
            )
        }

        HudCollapsiblePanel(
            title = "Music Source Management",
            sectionTag = "SOURCE SELECT",
            subtitle = "Choose folders, scan sources, and manage approved SAF paths.",
            expanded = sourceExpanded,
            onToggle = { sourceExpanded = !sourceExpanded },
            statusLabel = "${sources.size} SOURCES",
        ) {
            MusicSourceControls(
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
                compact = true,
            )
            if (permissionState != MediaPermissionState.Granted) {
                HudButton(
                    label = "Open app settings",
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        context.startActivity(intent)
                    },
                    accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
                )
            }
        }

        HudCollapsiblePanel(
            title = "Theme Select",
            sectionTag = "COMMAND DOCK",
            subtitle = "EVA themes first, then Ialemus originals.",
            expanded = themeExpanded,
            onToggle = { themeExpanded = !themeExpanded },
            statusLabel = selectedTheme.displayName.uppercase(),
        ) {
            ThemeGroupPanel(
                title = "EVA Themes",
                themes = ThemeId.evaThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
            ThemeGroupPanel(
                title = "Ialemus Original",
                themes = ThemeId.ialemusThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
        }

        HudCollapsiblePanel(
            title = "NAS / Bridge Connections",
            sectionTag = "NAS CONNECT",
            subtitle = "Configure LAN service URLs. Android connects via HTTP only — no shell/SSH/Docker.",
            expanded = nasExpanded,
            onToggle = { nasExpanded = !nasExpanded },
            statusLabel = if (nasSettings.bridgeConfigured) "CONFIGURED" else "SETUP",
        ) {
            OutlinedTextField(
                value = nasDisplayName,
                onValueChange = { nasDisplayName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("NAS display name") },
                placeholder = { Text("Ugreen NAS") },
                singleLine = true,
            )
            ConnectionModeSelector(
                selected = connectionMode,
                onSelect = { connectionMode = it },
            )
            OutlinedTextField(
                value = bridgeUrl,
                onValueChange = { bridgeUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Ialemus Bridge URL") },
                placeholder = { Text(NasUrlPlaceholders.BRIDGE) },
                singleLine = true,
            )
            OutlinedTextField(
                value = bridgeToken,
                onValueChange = { bridgeToken = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Bridge API token") },
                placeholder = { Text("Paste bridge token") },
                singleLine = true,
            )
            OutlinedTextField(
                value = meTubeUrl,
                onValueChange = { meTubeUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("MeTube URL") },
                placeholder = { Text(NasUrlPlaceholders.METUBE) },
                singleLine = true,
            )
            OutlinedTextField(
                value = slskdUrl,
                onValueChange = { slskdUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("slskd URL") },
                placeholder = { Text(NasUrlPlaceholders.SLSKD) },
                singleLine = true,
            )
            OutlinedTextField(
                value = jellyfinUrl,
                onValueChange = { jellyfinUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Jellyfin URL (optional)") },
                placeholder = { Text(NasUrlPlaceholders.JELLYFIN) },
                singleLine = true,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HudStatusChip(label = "Bridge · ${bridgeStatus.label}", highlighted = bridgeStatus == ConnectionTestStatus.REACHABLE)
                HudStatusChip(label = "MeTube · ${meTubeStatus.label}", highlighted = meTubeStatus == ConnectionTestStatus.REACHABLE)
                HudStatusChip(label = "slskd · ${slskdStatus.label}", highlighted = slskdStatus == ConnectionTestStatus.REACHABLE)
            }
            HudButton(
                label = "Save connections",
                onClick = {
                    settingsViewModel.saveNasConnectionSettings(
                        NasConnectionSettings(
                            nasDisplayName = nasDisplayName,
                            bridgeUrl = bridgeUrl,
                            bridgeToken = bridgeToken,
                            meTubeUrl = meTubeUrl,
                            slskdUrl = slskdUrl,
                            jellyfinUrl = jellyfinUrl,
                            connectionMode = connectionMode,
                        ),
                    )
                },
                modifier = Modifier.padding(top = 8.dp),
            )
            HudButton(
                label = "Test connections",
                onClick = {
                    settingsViewModel.saveNasConnectionSettings(
                        NasConnectionSettings(
                            nasDisplayName = nasDisplayName,
                            bridgeUrl = bridgeUrl,
                            bridgeToken = bridgeToken,
                            meTubeUrl = meTubeUrl,
                            slskdUrl = slskdUrl,
                            jellyfinUrl = jellyfinUrl,
                            connectionMode = connectionMode,
                        ),
                    )
                    settingsViewModel.testAllConnections()
                },
                modifier = Modifier.padding(top = 8.dp),
                accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
            )
            Text(
                text = "LAN HTTP is fine for local testing. Production bridge should use TLS where possible. Token storage is device-local DataStore (encrypted storage TODO).",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        HudCollapsiblePanel(
            title = "About",
            sectionTag = "PLAYBACK CORE",
            subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            expanded = aboutExpanded,
            onToggle = { aboutExpanded = !aboutExpanded },
            statusLabel = "MVP 1B.1",
        ) {
            Text(
                text = "Spotify-style dock polish, NAS connection settings, MeTube/slskd URL connectors, spotDL Bridge job scaffold.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
            Text(
                text = "Default theme: EVA-01 Berserk · Original EVA-inspired styling only.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ConnectionModeSelector(
    selected: ConnectionMode,
    onSelect: (ConnectionMode) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "CONNECTION MODE",
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.accentActive,
        )
        ConnectionMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(mode) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(mode.displayName, style = MaterialTheme.typography.bodySmall)
                HudStatusChip(
                    label = if (mode == selected) "ACTIVE" else "SELECT",
                    highlighted = mode == selected,
                )
            }
        }
    }
}

@Composable
private fun ThemeGroupPanel(
    title: String,
    themes: List<ThemeId>,
    selectedTheme: ThemeId,
    onSelect: (ThemeId) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = LocalIalemusTokens.current.glowColor,
        )
        themes.forEach { theme ->
            val selected = theme == selectedTheme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(theme) }
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) {
                            LocalIalemusTokens.current.accentActive
                        } else {
                            LocalIalemusTokens.current.hudBorderColor.copy(alpha = 0.35f)
                        },
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) {
                        LocalIalemusTokens.current.glowColor
                    } else {
                        LocalIalemusTokens.current.textMuted
                    },
                )
                HudStatusChip(
                    label = if (selected) "ACTIVE" else "SELECT",
                    highlighted = selected,
                )
            }
        }
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
