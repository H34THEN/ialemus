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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.settings.NasUrlPlaceholders
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudOutlinedTextField
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.PlaceholderCard
import com.heathen.ialemus.ui.components.ServiceWebCard
import com.heathen.ialemus.ui.screens.web.DockerWebService
import com.heathen.ialemus.ui.screens.web.ServiceWebViewState
import com.heathen.ialemus.ui.screens.web.ServiceWebViewScreen
import com.heathen.ialemus.ui.screens.web.openDockerServiceInApp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser

@Composable
fun DownloadsScreen(
    settingsViewModel: SettingsViewModel,
    onWebViewActive: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val nasSettings by settingsViewModel.nasConnectionSettings.collectAsStateWithLifecycle()
    val bridgeStatus by settingsViewModel.bridgeTestStatus.collectAsStateWithLifecycle()
    val meTubeStatus by settingsViewModel.meTubeTestStatus.collectAsStateWithLifecycle()
    val slskdStatus by settingsViewModel.slskdTestStatus.collectAsStateWithLifecycle()
    val nasUiStatus by settingsViewModel.nasUiTestStatus.collectAsStateWithLifecycle()
    val validationError by settingsViewModel.urlValidationError.collectAsStateWithLifecycle()

    var bridgeJobsExpanded by rememberSaveable { mutableStateOf(true) }
    var meTubeExpanded by rememberSaveable { mutableStateOf(true) }
    var slskdExpanded by rememberSaveable { mutableStateOf(false) }
    var nasUiExpanded by rememberSaveable { mutableStateOf(false) }
    var spotDlExpanded by rememberSaveable { mutableStateOf(false) }
    var completedExpanded by rememberSaveable { mutableStateOf(false) }
    var failedExpanded by rememberSaveable { mutableStateOf(false) }

    var activeServiceName by rememberSaveable { mutableStateOf<String?>(null) }
    var activeServiceUrl by rememberSaveable { mutableStateOf<String?>(null) }
    val activeWebView = if (activeServiceName != null && activeServiceUrl != null) {
        ServiceWebViewState(activeServiceName!!, activeServiceUrl!!)
    } else {
        null
    }

    var spotifyUrl by rememberSaveable { mutableStateOf("") }
    var playlistName by rememberSaveable { mutableStateOf("") }
    var generateM3u by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(activeWebView) {
        onWebViewActive(activeWebView != null)
    }

    if (activeWebView != null) {
        ServiceWebViewScreen(
            state = activeWebView,
            onClose = {
                activeServiceName = null
                activeServiceUrl = null
            },
            modifier = modifier,
        )
        return
    }

    val bridgeChip = if (nasSettings.bridgeConfigured) bridgeStatus.label else "Not configured"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HudHeader(
            title = "Downloads",
            statusLabel = "JOB QUEUE",
            subtitle = "Bridge jobs + NAS Docker modules (MeTube, slskd, NAS UI, spotDL)",
        )

        HudCollapsiblePanel(
            title = "Job Queue",
            sectionTag = "QUEUE SYNC",
            subtitle = "Active Ialemus Bridge jobs (GET /jobs).",
            expanded = bridgeJobsExpanded,
            onToggle = { bridgeJobsExpanded = !bridgeJobsExpanded },
            statusLabel = bridgeChip.uppercase(),
        ) {
            ConnectionStatusRow("Bridge", bridgeStatus, nasSettings.bridgeConfigured)
            HudStatusChip(label = "0 active jobs", disabled = true)
            PlaceholderCard(
                title = "No bridge jobs",
                body = "Job polling arrives with Bridge MVP 2.",
                sectionTag = "QUEUE SYNC",
            )
        }

        ServiceWebCard(
            title = "MeTube Module",
            sectionTag = "MODULE 02",
            subtitle = "Download videos/audio via MeTube Docker web UI.",
            savedUrl = nasSettings.meTubeUrl,
            urlPlaceholder = NasUrlPlaceholders.METUBE,
            status = meTubeStatus,
            onOpenInApp = {
                openDockerServiceInApp(DockerWebService.METUBE, nasSettings.meTubeUrl) { name, url ->
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
            modifier = Modifier.fillMaxWidth(),
        )

        ServiceWebCard(
            title = "slskd / Soulseek Module",
            sectionTag = "MODULE 03",
            subtitle = "Search and download via slskd Docker web UI.",
            savedUrl = nasSettings.slskdUrl,
            urlPlaceholder = NasUrlPlaceholders.SLSKD,
            status = slskdStatus,
            onOpenInApp = {
                openDockerServiceInApp(DockerWebService.SLSKD, nasSettings.slskdUrl) { name, url ->
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
            modifier = Modifier.fillMaxWidth(),
        )

        ServiceWebCard(
            title = "NAS UI Module",
            sectionTag = "NAS UI",
            subtitle = "Ugreen NAS management web UI (IP default, not baphomet.local).",
            savedUrl = nasSettings.nasUiUrl,
            urlPlaceholder = NasUrlPlaceholders.NAS_UI,
            status = nasUiStatus,
            onOpenInApp = {
                openDockerServiceInApp(DockerWebService.NAS_UI, nasSettings.nasUiUrl) { name, url ->
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
            modifier = Modifier.fillMaxWidth(),
        )

        HudCollapsiblePanel(
            title = "spotDL Module",
            sectionTag = "MODULE 01",
            subtitle = "Playlist + M3U/M3U8 jobs via Bridge — future.",
            expanded = spotDlExpanded,
            onToggle = { spotDlExpanded = !spotDlExpanded },
            statusLabel = if (nasSettings.bridgeConfigured) "BRIDGE READY" else "BRIDGE REQUIRED",
        ) {
            HudOutlinedTextField(
                value = spotifyUrl,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = "Spotify playlist/album/track URL",
                placeholder = "https://open.spotify.com/playlist/...",
                enabled = false,
            )
            HudOutlinedTextField(
                value = playlistName,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = "Playlist name",
                placeholder = "My Playlist",
                enabled = false,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("Generate M3U/M3U8", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                HudStatusChip(label = if (generateM3u) "ON" else "OFF", disabled = true)
            }
            HudStatusChip(
                label = "BRIDGE REQUIRED FOR COMMAND EXECUTION",
                disabled = true,
                modifier = Modifier.padding(top = 8.dp),
            )
            HudButton(
                label = "Submit to Bridge",
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "spotDL needs Ialemus Bridge. Android will not execute terminal/Docker commands.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        HudCollapsiblePanel(
            title = "Completed imports",
            sectionTag = "LOCAL SIGNAL",
            subtitle = "Finished downloads after bridge folder watch + rescan.",
            expanded = completedExpanded,
            onToggle = { completedExpanded = !completedExpanded },
            statusLabel = "EMPTY",
        ) {
            PlaceholderCard(
                title = "No completed imports",
                body = "Completed jobs will list here in MVP 2+.",
                sectionTag = "QUEUE SYNC",
            )
        }

        HudCollapsiblePanel(
            title = "Failed jobs",
            sectionTag = "ALERT",
            subtitle = "No failed jobs.",
            expanded = failedExpanded,
            onToggle = { failedExpanded = !failedExpanded },
            statusLabel = "CLEAR",
        ) {
            HudStatusChip(label = "No failures", disabled = true)
        }
    }
}

@Composable
private fun ConnectionStatusRow(
    service: String,
    status: ConnectionTestStatus,
    configured: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HudStatusChip(
            label = "$service · ${if (configured) status.label else ConnectionTestStatus.NOT_CONFIGURED.label}",
            highlighted = status == ConnectionTestStatus.REACHABLE,
            disabled = !configured,
        )
    }
}
