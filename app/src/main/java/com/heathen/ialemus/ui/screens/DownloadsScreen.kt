package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.PlaceholderCard
import com.heathen.ialemus.ui.theme.screenHorizontalPadding

@Composable
fun DownloadsScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val horizontalPad = screenHorizontalPadding()
    val nasSettings by settingsViewModel.nasConnectionSettings.collectAsStateWithLifecycle()
    val bridgeStatus by settingsViewModel.bridgeTestStatus.collectAsStateWithLifecycle()
    val meTubeStatus by settingsViewModel.meTubeTestStatus.collectAsStateWithLifecycle()
    val slskdStatus by settingsViewModel.slskdTestStatus.collectAsStateWithLifecycle()

    var bridgeJobsExpanded by rememberSaveable { mutableStateOf(true) }
    var meTubeExpanded by rememberSaveable { mutableStateOf(false) }
    var slskdExpanded by rememberSaveable { mutableStateOf(false) }
    var spotDlExpanded by rememberSaveable { mutableStateOf(false) }
    var completedExpanded by rememberSaveable { mutableStateOf(false) }
    var failedExpanded by rememberSaveable { mutableStateOf(false) }

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
            subtitle = "Bridge import monitor — MVP 2+",
        )

        HudCollapsiblePanel(
            title = "Bridge jobs",
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

        HudCollapsiblePanel(
            title = "MeTube imports",
            sectionTag = "MODULE 02",
            subtitle = "MeTube download jobs via Bridge.",
            expanded = meTubeExpanded,
            onToggle = { meTubeExpanded = !meTubeExpanded },
            statusLabel = meTubeStatus.label.uppercase(),
        ) {
            ConnectionStatusRow("MeTube", meTubeStatus, nasSettings.meTubeConfigured)
            PlaceholderCard(
                title = "No MeTube imports",
                body = "Configure MeTube URL in Acquire or Settings.",
                sectionTag = "METUBE",
            )
        }

        HudCollapsiblePanel(
            title = "slskd downloads",
            sectionTag = "MODULE 03",
            subtitle = "Soulseek downloads via Bridge.",
            expanded = slskdExpanded,
            onToggle = { slskdExpanded = !slskdExpanded },
            statusLabel = slskdStatus.label.uppercase(),
        ) {
            ConnectionStatusRow("slskd", slskdStatus, nasSettings.slskdConfigured)
            PlaceholderCard(
                title = "No slskd downloads",
                body = "Configure slskd URL in Acquire or Settings.",
                sectionTag = "SLSKD",
            )
        }

        HudCollapsiblePanel(
            title = "spotDL playlist jobs",
            sectionTag = "MODULE 01",
            subtitle = "Playlist + M3U/M3U8 jobs via Bridge.",
            expanded = spotDlExpanded,
            onToggle = { spotDlExpanded = !spotDlExpanded },
            statusLabel = if (nasSettings.bridgeConfigured) "BRIDGE READY" else "BRIDGE REQUIRED",
        ) {
            HudStatusChip(
                label = if (nasSettings.bridgeConfigured) "Bridge configured" else "Bridge required",
                highlighted = nasSettings.bridgeConfigured,
                disabled = !nasSettings.bridgeConfigured,
            )
            PlaceholderCard(
                title = "No spotDL jobs",
                body = "Submit playlist jobs from Acquire once Bridge endpoints ship.",
                sectionTag = "SPOTDL",
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
