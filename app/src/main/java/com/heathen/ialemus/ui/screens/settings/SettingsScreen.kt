package com.heathen.ialemus.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.library.MediaPermissionState
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.model.ConnectionMode
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.model.NowPlayingVisualizerMode
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.settings.LocalServiceDefaults
import com.heathen.ialemus.core.settings.NasConnectionSettings
import com.heathen.ialemus.core.settings.NasUrlPlaceholders
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.spotify.SpotifyAppStatus
import com.heathen.ialemus.core.spotify.SpotifyDefaults
import com.heathen.ialemus.core.spotify.SpotifyRemoteConnectionState
import com.heathen.ialemus.core.spotify.SpotifyViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudOutlinedTextField
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.MusicSourceControls
import com.heathen.ialemus.ui.util.openSpotifyApp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.previewColorsFor
import com.heathen.ialemus.ui.theme.screenHorizontalPadding

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    spotifyViewModel: SpotifyViewModel,
    libraryViewModel: LibraryViewModel,
    onOpenSpotifyExperimental: () -> Unit = {},
    onRequestRecordAudioPermission: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()
    val showMiniPlayerBar by settingsViewModel.showMiniPlayerBar.collectAsStateWithLifecycle()
    val nowPlayingLayoutMode by settingsViewModel.nowPlayingLayoutMode.collectAsStateWithLifecycle()
    val nowPlayingVisualizerMode by settingsViewModel.nowPlayingVisualizerMode.collectAsStateWithLifecycle()
    val reactiveVisualizerEnabled by settingsViewModel.reactiveVisualizerEnabled.collectAsStateWithLifecycle()
    val spotifyUi by spotifyViewModel.uiState.collectAsStateWithLifecycle()
    val trackCount by settingsViewModel.trackCount.collectAsStateWithLifecycle()
    val nasSettings by settingsViewModel.nasConnectionSettings.collectAsStateWithLifecycle()
    val bridgeStatus by settingsViewModel.bridgeTestStatus.collectAsStateWithLifecycle()
    val meTubeStatus by settingsViewModel.meTubeTestStatus.collectAsStateWithLifecycle()
    val slskdStatus by settingsViewModel.slskdTestStatus.collectAsStateWithLifecycle()
    val nasUiStatus by settingsViewModel.nasUiTestStatus.collectAsStateWithLifecycle()
    val validationError by settingsViewModel.urlValidationError.collectAsStateWithLifecycle()
    val permissionState by libraryViewModel.permissionState.collectAsStateWithLifecycle()
    val scanState by libraryViewModel.scanState.collectAsStateWithLifecycle()
    val sources by libraryViewModel.librarySources.collectAsStateWithLifecycle()
    var playbackExpanded by rememberSaveable { mutableStateOf(false) }
    var experimentalExpanded by rememberSaveable { mutableStateOf(false) }
    var sourceExpanded by rememberSaveable { mutableStateOf(false) }
    var nasExpanded by rememberSaveable { mutableStateOf(false) }
    var libraryExpanded by rememberSaveable { mutableStateOf(false) }
    var aboutExpanded by rememberSaveable { mutableStateOf(false) }
    var pendingFullDeviceScan by rememberSaveable { mutableStateOf(false) }

    var nasDisplayName by rememberSaveable(nasSettings.nasDisplayName) { mutableStateOf(nasSettings.nasDisplayName) }
    var bridgeToken by rememberSaveable(nasSettings.bridgeToken) { mutableStateOf(nasSettings.bridgeToken) }
    var meTubeUrl by rememberSaveable(nasSettings.meTubeUrl) {
        mutableStateOf(nasSettings.meTubeUrl.ifBlank { LocalServiceDefaults.METUBE })
    }
    var slskdUrl by rememberSaveable(nasSettings.slskdUrl) {
        mutableStateOf(nasSettings.slskdUrl.ifBlank { LocalServiceDefaults.SLSKD })
    }
    var nasUiUrl by rememberSaveable(nasSettings.nasUiUrl) {
        mutableStateOf(nasSettings.nasUiUrl.ifBlank { LocalServiceDefaults.NAS_UI })
    }
    var bridgeUrl by rememberSaveable(nasSettings.bridgeUrl) {
        mutableStateOf(nasSettings.bridgeUrl.ifBlank { LocalServiceDefaults.BRIDGE_FUTURE })
    }
    var connectionMode by rememberSaveable(nasSettings.connectionMode.name) {
        mutableStateOf(nasSettings.connectionMode)
    }
    var spotifyClientIdOverride by rememberSaveable { mutableStateOf("") }
    var showAdvancedSpotify by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(experimentalExpanded) {
        if (experimentalExpanded) {
            spotifyViewModel.refreshSpotifyAppStatus(context)
        }
    }

    val isScanning = scanState is LibraryScanState.ScanningFolders ||
        scanState is LibraryScanState.ScanningFullDevice

    val saveDockerUrls: () -> Unit = {
        settingsViewModel.saveNasConnectionSettings(
            nasSettings.copy(
                nasDisplayName = nasDisplayName,
                bridgeUrl = bridgeUrl,
                bridgeToken = bridgeToken,
                meTubeUrl = meTubeUrl,
                slskdUrl = slskdUrl,
                nasUiUrl = nasUiUrl,
                connectionMode = connectionMode,
            ),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HudHeader(
            title = "Settings",
            statusLabel = "RC POLISH",
            subtitle = "Ialemus ${BuildConfig.VERSION_NAME} · Release Candidate Polish / MVP 1B.10",
        )

        HudCollapsiblePanel(
            title = "Visual / Playback UI",
            sectionTag = "NOW PLAYING",
            subtitle = "Mini player, layout modes, themes, and DAP mode.",
            expanded = playbackExpanded,
            onToggle = { playbackExpanded = !playbackExpanded },
            statusLabel = nowPlayingLayoutMode.displayName.uppercase(),
        ) {
            RowSwitch(
                label = "Show bottom mini player",
                checked = showMiniPlayerBar,
                onCheckedChange = settingsViewModel::setShowMiniPlayerBar,
            )
            Text(
                text = "Show Spotify-style playback controls above the command dock outside Now Playing.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            RowSwitch(
                label = "DAP low-power HUD (reduced grid/scanlines)",
                checked = dapMode,
                onCheckedChange = settingsViewModel::setDapMode,
            )
            Text(
                text = "NOW PLAYING LAYOUT",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.accentActive,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            NowPlayingLayoutSelector(
                selected = nowPlayingLayoutMode,
                onSelect = settingsViewModel::setNowPlayingLayoutMode,
            )
            Text(
                text = "CYBERPUNK VISUALIZER",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.accentActive,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
            NowPlayingVisualizerSelector(
                selected = nowPlayingVisualizerMode,
                dapMode = dapMode,
                onSelect = settingsViewModel::setNowPlayingVisualizerMode,
            )
            RowSwitch(
                label = "Reactive audio visualizer (optional RECORD_AUDIO for session capture — not mic input)",
                checked = reactiveVisualizerEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) onRequestRecordAudioPermission()
                    else settingsViewModel.setReactiveVisualizerEnabled(false)
                },
            )
            Text(
                text = "Needed only for reactive audio visualizer. Music playback works without it. " +
                    "Falls back to simulated signal if unavailable.",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = "THEME SELECT",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.accentActive,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
            ThemeGroupPanel(
                title = "EVA Themes",
                themes = ThemeId.evaThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
            ThemeGroupPanel(
                title = "Ialemus Original Themes",
                themes = ThemeId.ialemusThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
        }

        HudCollapsiblePanel(
            title = "Experimental / Deprecated",
            sectionTag = "PAUSED",
            subtitle = "Features paused while Local Core and NAS tools are the focus.",
            expanded = experimentalExpanded,
            onToggle = { experimentalExpanded = !experimentalExpanded },
            statusLabel = "SPOTIFY PAUSED",
        ) {
            Text(
                text = "Spotify Remote is paused for now. Local Core and NAS tools are the current focus. " +
                    "Code remains for future work — not removed.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.warningColor,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, tokens.hudBorderColor.copy(alpha = 0.35f), MaterialTheme.shapes.small)
                    .padding(10.dp),
            ) {
                Text(
                    text = "SPOTIFY REMOTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.accentActive,
                )
                Text(
                    text = "Paused · experimental",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )
                HudStatusChip(label = "PAUSED / EXPERIMENTAL", warning = true)
                HudButton(
                    label = "Open Spotify Remote screen",
                    onClick = onOpenSpotifyExperimental,
                    modifier = Modifier.padding(top = 8.dp),
                    accent = HudButtonAccent.Neutral,
                )
                Text(
                    text = "Spotify Remote is paused for now. Spotify Remote code remains for future work.",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
                HudStatusChip(
                    label = spotifyUi.spotifyAppStatus.label.uppercase(),
                    highlighted = spotifyUi.spotifyAppStatus == SpotifyAppStatus.INSTALLED,
                    modifier = Modifier.padding(top = 8.dp),
                )
                HudStatusChip(
                    label = "REMOTE: ${spotifyUi.remoteConnectionState.label.uppercase()}",
                    highlighted = spotifyUi.remoteConnectionState == SpotifyRemoteConnectionState.CONNECTED,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (showAdvancedSpotify) {
                    Text(
                        text = "Client ID: ${spotifyUi.clientId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        text = "Redirect URI: ${spotifyUi.redirectUri}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudButton(
                        label = "Open Spotify App",
                        onClick = { openSpotifyApp(context) },
                        modifier = Modifier.weight(1f),
                        accent = HudButtonAccent.Neutral,
                    )
                    HudButton(
                        label = "Recheck app",
                        onClick = { spotifyViewModel.refreshSpotifyAppStatus(context) },
                        modifier = Modifier.weight(1f),
                        accent = HudButtonAccent.Neutral,
                    )
                }
                HudButton(
                    label = if (showAdvancedSpotify) "Hide advanced" else "Advanced (Client ID)",
                    onClick = { showAdvancedSpotify = !showAdvancedSpotify },
                    modifier = Modifier.padding(top = 8.dp),
                    accent = HudButtonAccent.Neutral,
                )
                if (showAdvancedSpotify) {
                    HudOutlinedTextField(
                        value = spotifyClientIdOverride.ifBlank { spotifyUi.clientId },
                        onValueChange = { spotifyClientIdOverride = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = "Override Client ID",
                        placeholder = SpotifyDefaults.CLIENT_ID,
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HudButton(
                            label = "Login",
                            onClick = { spotifyViewModel.startLogin(context) },
                            modifier = Modifier.weight(1f),
                        )
                        HudButton(
                            label = "Reset auth",
                            onClick = spotifyViewModel::logout,
                            modifier = Modifier.weight(1f),
                            accent = HudButtonAccent.Neutral,
                        )
                    }
                    HudButton(
                        label = "Reset Spotify defaults",
                        onClick = spotifyViewModel::resetDefaults,
                        modifier = Modifier.padding(top = 8.dp),
                        accent = HudButtonAccent.Neutral,
                    )
                }
            }
        }

        HudCollapsiblePanel(
            title = "Local Library",
            sectionTag = "LOCAL SIGNAL",
            subtitle = "$trackCount tracks indexed on device.",
            expanded = libraryExpanded,
            onToggle = { libraryExpanded = !libraryExpanded },
            statusLabel = "$trackCount TRACKS",
        ) {
            HudStatusChip(label = "$trackCount tracks", highlighted = trackCount > 0)
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
            title = "NAS / Docker Web UIs",
            sectionTag = "DOCKER WEB",
            subtitle = "Configure URLs here; open modules from Downloads.",
            expanded = nasExpanded,
            onToggle = { nasExpanded = !nasExpanded },
            statusLabel = if (nasSettings.meTubeConfigured || nasSettings.slskdConfigured) "READY" else "SETUP",
        ) {
            HudOutlinedTextField(
                value = meTubeUrl,
                onValueChange = {
                    meTubeUrl = it
                    settingsViewModel.clearValidationError()
                },
                modifier = Modifier.fillMaxWidth(),
                label = "MeTube URL",
                placeholder = NasUrlPlaceholders.METUBE,
                isError = validationError?.contains("MeTube") == true,
            )
            HudOutlinedTextField(
                value = slskdUrl,
                onValueChange = {
                    slskdUrl = it
                    settingsViewModel.clearValidationError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = "slskd URL",
                placeholder = NasUrlPlaceholders.SLSKD,
                isError = validationError?.contains("slskd") == true,
            )
            HudOutlinedTextField(
                value = nasUiUrl,
                onValueChange = {
                    nasUiUrl = it
                    settingsViewModel.clearValidationError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = "Ugreen NAS UI URL",
                placeholder = NasUrlPlaceholders.NAS_UI,
                isError = validationError?.contains("NAS UI") == true,
            )
            HudOutlinedTextField(
                value = bridgeUrl,
                onValueChange = {
                    bridgeUrl = it
                    settingsViewModel.clearValidationError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = "Ialemus Bridge URL (future — not required)",
                placeholder = NasUrlPlaceholders.BRIDGE,
                isError = validationError?.contains("Bridge") == true,
            )
            if (validationError != null) {
                Text(
                    text = validationError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.warningColor,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HudStatusChip(label = "MeTube · ${meTubeStatus.label}", highlighted = meTubeStatus == ConnectionTestStatus.REACHABLE)
                HudStatusChip(label = "slskd · ${slskdStatus.label}", highlighted = slskdStatus == ConnectionTestStatus.REACHABLE)
                HudStatusChip(label = "NAS UI · ${nasUiStatus.label}", highlighted = nasUiStatus == ConnectionTestStatus.REACHABLE)
            }
            HudButton(
                label = "Save",
                onClick = saveDockerUrls,
                modifier = Modifier.padding(top = 8.dp),
            )
            HudButton(
                label = "Reset to local defaults",
                onClick = {
                    settingsViewModel.resetToLocalDefaults()
                    val defaults = LocalServiceDefaults.asSettings()
                    meTubeUrl = defaults.meTubeUrl
                    slskdUrl = defaults.slskdUrl
                    nasUiUrl = defaults.nasUiUrl
                    bridgeUrl = defaults.bridgeUrl
                },
                modifier = Modifier.padding(top = 8.dp),
                accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Test MeTube",
                onClick = {
                    saveDockerUrls()
                    settingsViewModel.testMeTubeConnection()
                },
                modifier = Modifier.padding(top = 8.dp),
                accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Test slskd",
                onClick = {
                    saveDockerUrls()
                    settingsViewModel.testSlskdConnection()
                },
                modifier = Modifier.padding(top = 8.dp),
                accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Test NAS UI",
                onClick = {
                    saveDockerUrls()
                    settingsViewModel.testNasUiConnection()
                },
                modifier = Modifier.padding(top = 8.dp),
                accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral,
            )
            Text(
                text = "Open services from Acquire → Open in Ialemus. WebView sessions are not stored by Ialemus.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        HudCollapsiblePanel(
            title = "About",
            sectionTag = "IALEMUS",
            subtitle = "Ialemus ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            expanded = aboutExpanded,
            onToggle = { aboutExpanded = !aboutExpanded },
            statusLabel = "RC 1B.10",
        ) {
            val sources by libraryViewModel.librarySources.collectAsStateWithLifecycle()
            Text(
                text = "Ialemus — cyberpunk DAP music player for local libraries and NAS tools.",
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.accentActive,
            )
            Text(
                text = "Build: Release Candidate Polish / MVP 1B.10",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.glowColor,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "Library index: $trackCount tracks · ${sources.size} sources in Room",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.glowColor,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "Tracks persist in Room across restarts. Rescan updates index; failed scans no longer wipe your library.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "MVP 1B.10: new app icon, black splash, RC branding, HiBy layout hardening, startup polish.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun NowPlayingVisualizerSelector(
    selected: NowPlayingVisualizerMode,
    dapMode: Boolean,
    onSelect: (NowPlayingVisualizerMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        NowPlayingVisualizerMode.entries.forEach { mode ->
            val isSelected = mode == selected
            HudButton(
                label = mode.label + if (dapMode && mode != NowPlayingVisualizerMode.STATIC_HUD) " (DAP → Static)" else "",
                onClick = { onSelect(mode) },
                accent = if (isSelected) HudButtonAccent.Primary else HudButtonAccent.Neutral,
            )
        }
    }
}

@Composable
private fun NowPlayingLayoutSelector(
    selected: NowPlayingLayoutMode,
    onSelect: (NowPlayingLayoutMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NowPlayingLayoutMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(mode) }
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) {
                            LocalIalemusTokens.current.accentActive
                        } else {
                            LocalIalemusTokens.current.hudBorderColor.copy(alpha = 0.35f)
                        },
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            LocalIalemusTokens.current.glowColor
                        } else {
                            LocalIalemusTokens.current.textMuted
                        },
                    )
                    Text(
                        text = mode.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalIalemusTokens.current.textMuted,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                HudStatusChip(
                    label = if (isSelected) "ACTIVE" else "SELECT",
                    highlighted = isSelected,
                )
            }
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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = theme.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) {
                            LocalIalemusTokens.current.glowColor
                        } else {
                            LocalIalemusTokens.current.textMuted
                        },
                    )
                    ThemePreviewDots(theme = theme, modifier = Modifier.padding(top = 6.dp))
                }
                HudStatusChip(
                    label = if (selected) "ACTIVE" else "SELECT",
                    highlighted = selected,
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewDots(
    theme: ThemeId,
    modifier: Modifier = Modifier,
) {
    val colors = previewColorsFor(theme)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, MaterialTheme.shapes.extraSmall)
                    .border(1.dp, LocalIalemusTokens.current.hudBorderColor.copy(alpha = 0.35f), MaterialTheme.shapes.extraSmall),
            )
        }
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val tokens = LocalIalemusTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.textPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = tokens.accentActive,
                checkedTrackColor = tokens.accentActive.copy(alpha = 0.35f),
                uncheckedThumbColor = tokens.textMuted,
                uncheckedTrackColor = tokens.hudBorderColor.copy(alpha = 0.45f),
            ),
        )
    }
}
