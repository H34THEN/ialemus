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
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.settings.LocalServiceDefaults
import com.heathen.ialemus.core.settings.NasConnectionSettings
import com.heathen.ialemus.core.settings.NasUrlPlaceholders
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.spotify.SpotifyAppStatus
import com.heathen.ialemus.core.spotify.SpotifyConnectionStatus
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
import com.heathen.ialemus.ui.util.openSpotifyWeb
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    spotifyViewModel: SpotifyViewModel,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val selectedTheme by settingsViewModel.themeId.collectAsStateWithLifecycle()
    val dapMode by settingsViewModel.dapModeEnabled.collectAsStateWithLifecycle()
    val showMiniPlayerBar by settingsViewModel.showMiniPlayerBar.collectAsStateWithLifecycle()
    val nowPlayingLayoutMode by settingsViewModel.nowPlayingLayoutMode.collectAsStateWithLifecycle()
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
    var playbackExpanded by rememberSaveable { mutableStateOf(true) }
    var spotifyExpanded by rememberSaveable { mutableStateOf(false) }
    var sourceExpanded by rememberSaveable { mutableStateOf(false) }
    var nasExpanded by rememberSaveable { mutableStateOf(false) }
    var libraryExpanded by rememberSaveable { mutableStateOf(true) }
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

    LaunchedEffect(Unit) {
        spotifyViewModel.refreshSpotifyAppStatus(context)
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HudHeader(
            title = "Settings",
            statusLabel = "DAP MODE",
            subtitle = "Ialemus MVP 1B.6 · Spotify App Remote",
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
                title = "Ialemus Original",
                themes = ThemeId.ialemusThemes,
                selectedTheme = selectedTheme,
                onSelect = settingsViewModel::setTheme,
            )
        }

        HudCollapsiblePanel(
            title = "Spotify Integration",
            sectionTag = "SPOTIFY REMOTE",
            subtitle = "App Remote + PKCE — playback via Spotify app, not Local Core.",
            expanded = spotifyExpanded,
            onToggle = { spotifyExpanded = !spotifyExpanded },
            statusLabel = spotifyUi.connectionStatus.label.uppercase(),
        ) {
            HudStatusChip(
                label = spotifyUi.connectionStatus.label.uppercase(),
                highlighted = spotifyUi.connectionStatus == SpotifyConnectionStatus.CONNECTED,
                warning = spotifyUi.sessionExpired || spotifyUi.connectionStatus == SpotifyConnectionStatus.ERROR,
            )
            HudStatusChip(
                label = spotifyUi.spotifyAppStatus.label.uppercase(),
                highlighted = spotifyUi.spotifyAppStatus == SpotifyAppStatus.INSTALLED,
                warning = spotifyUi.spotifyAppStatus == SpotifyAppStatus.NOT_INSTALLED,
                modifier = Modifier.padding(top = 4.dp),
            )
            HudStatusChip(
                label = "REMOTE: ${spotifyUi.remoteConnectionState.label.uppercase()}",
                highlighted = spotifyUi.remoteConnectionState == SpotifyRemoteConnectionState.CONNECTED,
                warning = spotifyUi.remoteConnectionState == SpotifyRemoteConnectionState.ERROR,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "Client ID: ${spotifyUi.clientId}",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textPrimary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "Redirect URI: ${spotifyUi.redirectUri}",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
            Text(
                text = "Ialemus uses Spotify PKCE login. Do not enter a Client Secret.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.warningColor,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "Login: ${if (spotifyUi.hasRefreshToken) "logged in" else "not logged in"} · " +
                    if (spotifyUi.tokenExpiresAtMs > 0) {
                        "token expires ${java.text.DateFormat.getDateTimeInstance().format(spotifyUi.tokenExpiresAtMs)}"
                    } else {
                        "no token"
                    },
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
            spotifyUi.errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.warningColor, modifier = Modifier.padding(top = 4.dp))
            }
            spotifyUi.remoteErrorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.warningColor, modifier = Modifier.padding(top = 4.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HudButton(
                    label = "Login with Spotify",
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
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HudButton(
                    label = "Open Spotify App",
                    onClick = { openSpotifyApp(context) },
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Neutral,
                )
                HudButton(
                    label = "Recheck Spotify App",
                    onClick = { spotifyViewModel.refreshSpotifyAppStatus(context) },
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Neutral,
                )
            }
            HudButton(
                label = "Connect Spotify Remote",
                onClick = { spotifyViewModel.connectSpotifyRemote(context) },
                enabled = spotifyUi.spotifyAppStatus == SpotifyAppStatus.INSTALLED,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (spotifyUi.spotifyAppStatus == SpotifyAppStatus.NOT_INSTALLED) {
                HudButton(
                    label = "Open Spotify Web",
                    onClick = { openSpotifyWeb(context) },
                    modifier = Modifier.padding(top = 8.dp),
                    accent = HudButtonAccent.Neutral,
                )
            }
            HudButton(label = "Reset Spotify defaults", onClick = spotifyViewModel::resetDefaults, modifier = Modifier.padding(top = 8.dp), accent = HudButtonAccent.Neutral)
            HudButton(label = if (showAdvancedSpotify) "Hide advanced" else "Advanced override", onClick = { showAdvancedSpotify = !showAdvancedSpotify }, modifier = Modifier.padding(top = 8.dp), accent = HudButtonAccent.Neutral)
            if (showAdvancedSpotify) {
                HudOutlinedTextField(
                    value = spotifyClientIdOverride.ifBlank { spotifyUi.clientId },
                    onValueChange = { spotifyClientIdOverride = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = "Override Client ID",
                    placeholder = SpotifyDefaults.CLIENT_ID,
                )
                HudButton(
                    label = "Save Client ID override",
                    onClick = { spotifyViewModel.saveClientIdOverride(spotifyClientIdOverride.ifBlank { spotifyUi.clientId }) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Text(
                text = "LOCAL CORE: Ialemus ExoPlayer for local files. SPOTIFY REMOTE: Spotify app on HiBy R4 controlled via App Remote and Web API.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
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
            sectionTag = "PLAYBACK CORE",
            subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            expanded = aboutExpanded,
            onToggle = { aboutExpanded = !aboutExpanded },
            statusLabel = "MVP 1B.6",
        ) {
            Text(
                text = "Spotify App Remote device activation, Web API Connect device list/transfer, and HiBy R4 Spotify setup flow.",
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
