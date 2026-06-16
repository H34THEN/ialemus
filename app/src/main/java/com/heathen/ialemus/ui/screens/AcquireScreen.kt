package com.heathen.ialemus.ui.screens

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.network.BridgePlaceholder
import com.heathen.ialemus.core.network.ServiceUrlValidator
import com.heathen.ialemus.core.settings.LocalServiceDefaults
import com.heathen.ialemus.core.settings.NasUrlPlaceholders
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.ServiceWebCard
import com.heathen.ialemus.ui.screens.web.DockerWebService
import com.heathen.ialemus.ui.screens.web.ServiceWebViewState
import com.heathen.ialemus.ui.screens.web.ServiceWebViewScreen
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser

@Composable
fun AcquireScreen(
    settingsViewModel: SettingsViewModel,
    onWebViewActive: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val nasSettings by settingsViewModel.nasConnectionSettings.collectAsStateWithLifecycle()
    val meTubeStatus by settingsViewModel.meTubeTestStatus.collectAsStateWithLifecycle()
    val slskdStatus by settingsViewModel.slskdTestStatus.collectAsStateWithLifecycle()
    val nasUiStatus by settingsViewModel.nasUiTestStatus.collectAsStateWithLifecycle()
    val validationError by settingsViewModel.urlValidationError.collectAsStateWithLifecycle()

    var archNoteExpanded by rememberSaveable { mutableStateOf(false) }
    var activeServiceName by rememberSaveable { mutableStateOf<String?>(null) }
    var activeServiceUrl by rememberSaveable { mutableStateOf<String?>(null) }
    val activeWebView = if (activeServiceName != null && activeServiceUrl != null) {
        ServiceWebViewState(activeServiceName!!, activeServiceUrl!!)
    } else {
        null
    }

    var spotifyUrl by rememberSaveable { mutableStateOf("") }
    var playlistName by rememberSaveable { mutableStateOf("") }
    var spotDlFormat by rememberSaveable { mutableStateOf("m4a") }
    var outputTarget by rememberSaveable { mutableStateOf("nas_music_playlists") }
    var generateM3u by rememberSaveable { mutableStateOf(true) }
    var skipExisting by rememberSaveable { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(activeWebView) {
        onWebViewActive(activeWebView != null)
    }

    if (activeWebView != null) {
        ServiceWebViewScreen(
            state = activeWebView!!,
            onClose = {
                activeServiceName = null
                activeServiceUrl = null
            },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HudHeader(
            title = "Acquire",
            statusLabel = "DOCKER WEB UI",
            subtitle = "Wrap running NAS containers · no Bridge required yet",
        )

        HudCollapsiblePanel(
            title = "Bridge note",
            sectionTag = "FUTURE",
            subtitle = "spotDL jobs will use Ialemus Bridge later. MeTube/slskd open via web UI now.",
            expanded = archNoteExpanded,
            onToggle = { archNoteExpanded = !archNoteExpanded },
            statusLabel = "OPTIONAL",
        ) {
            Text(
                text = BridgePlaceholder.ARCHITECTURE_NOTE,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
        }

        ServiceWebCard(
            title = DockerWebService.METUBE.displayName,
            sectionTag = "MODULE 02",
            subtitle = "Download videos/audio via the running MeTube Docker web UI.",
            savedUrl = nasSettings.meTubeUrl,
            urlPlaceholder = NasUrlPlaceholders.METUBE,
            status = meTubeStatus,
            onOpenInApp = {
                openServiceInApp(DockerWebService.METUBE, nasSettings.meTubeUrl) { name, url ->
                    activeServiceName = name
                    activeServiceUrl = url
                }
            },
            onOpenExternal = { openUrlInBrowser(context, nasSettings.meTubeUrl) },
            onTestConnection = settingsViewModel::testMeTubeConnection,
            onSaveUrl = { url ->
                settingsViewModel.saveServiceUrl(nasSettings, meTubeUrl = url)
            },
            validationError = validationError,
        )

        ServiceWebCard(
            title = DockerWebService.SLSKD.displayName,
            sectionTag = "MODULE 03",
            subtitle = "Search and download via the running slskd Docker web UI.",
            savedUrl = nasSettings.slskdUrl,
            urlPlaceholder = NasUrlPlaceholders.SLSKD,
            status = slskdStatus,
            onOpenInApp = {
                openServiceInApp(DockerWebService.SLSKD, nasSettings.slskdUrl) { name, url ->
                    activeServiceName = name
                    activeServiceUrl = url
                }
            },
            onOpenExternal = { openUrlInBrowser(context, nasSettings.slskdUrl) },
            onTestConnection = settingsViewModel::testSlskdConnection,
            onSaveUrl = { url ->
                settingsViewModel.saveServiceUrl(nasSettings, slskdUrl = url)
            },
            validationError = validationError,
        )

        ServiceWebCard(
            title = DockerWebService.NAS_UI.displayName,
            sectionTag = "NAS UI",
            subtitle = "Open the Ugreen NAS management web UI when needed.",
            savedUrl = nasSettings.nasUiUrl,
            urlPlaceholder = NasUrlPlaceholders.NAS_UI,
            status = nasUiStatus,
            onOpenInApp = {
                openServiceInApp(DockerWebService.NAS_UI, nasSettings.nasUiUrl) { name, url ->
                    activeServiceName = name
                    activeServiceUrl = url
                }
            },
            onOpenExternal = { openUrlInBrowser(context, nasSettings.nasUiUrl) },
            onTestConnection = settingsViewModel::testNasUiConnection,
            onSaveUrl = { url ->
                settingsViewModel.saveServiceUrl(nasSettings, nasUiUrl = url)
            },
            validationError = validationError,
        )

        HudPanel(
            title = "spotDL",
            sectionTag = "MODULE 01",
            subtitle = "Future playlist jobs — Bridge or NAS job runner required.",
        ) {
            OutlinedTextField(
                value = spotifyUrl,
                onValueChange = { spotifyUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Spotify playlist/album/track URL") },
                placeholder = { Text("https://open.spotify.com/playlist/...") },
                singleLine = true,
                enabled = false,
            )
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Playlist name") },
                placeholder = { Text("My Playlist") },
                singleLine = true,
                enabled = false,
            )
            RowSwitch(
                label = "Generate M3U/M3U8",
                checked = generateM3u,
                onCheckedChange = { },
                enabled = false,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                HudStatusChip(
                    label = "BRIDGE REQUIRED FOR COMMAND EXECUTION",
                    disabled = true,
                )
            }
            HudButton(
                label = "Submit to Bridge",
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "spotDL needs Ialemus Bridge or a NAS-side job runner. Android will not execute terminal/Docker commands.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private fun openServiceInApp(
    service: DockerWebService,
    savedUrl: String,
    onOpen: (String, String) -> Unit,
) {
    val raw = savedUrl.ifBlank { defaultUrlFor(service) }
    val url = ServiceUrlValidator.normalizeForLoad(raw)
    if (url.isBlank()) return
    onOpen(service.displayName, url)
}

private fun defaultUrlFor(service: DockerWebService): String = when (service) {
    DockerWebService.METUBE -> LocalServiceDefaults.METUBE
    DockerWebService.SLSKD -> LocalServiceDefaults.SLSKD
    DockerWebService.NAS_UI -> LocalServiceDefaults.NAS_UI
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
