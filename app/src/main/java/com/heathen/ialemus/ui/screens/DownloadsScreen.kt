package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.PlaceholderCard

@Composable
fun DownloadsScreen(modifier: Modifier = Modifier) {
    var queueExpanded by rememberSaveable { mutableStateOf(true) }
    var completedExpanded by rememberSaveable { mutableStateOf(false) }
    var failedExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HudHeader(
            title = "Downloads",
            statusLabel = "JOB QUEUE",
            subtitle = "Bridge import monitor — MVP 3",
        )

        HudCollapsiblePanel(
            title = "Job queue",
            sectionTag = "QUEUE SYNC",
            subtitle = "Active bridge jobs (GET /jobs) will appear here in MVP 3.",
            expanded = queueExpanded,
            onToggle = { queueExpanded = !queueExpanded },
            statusLabel = "0 ACTIVE",
        ) {
            HudStatusChip(label = "0 active jobs", disabled = true)
        }

        HudCollapsiblePanel(
            title = "Completed imports",
            sectionTag = "LOCAL SIGNAL",
            subtitle = "Finished spot-dl, MeTube, and slskd downloads after bridge folder watch + rescan.",
            expanded = completedExpanded,
            onToggle = { completedExpanded = !completedExpanded },
            statusLabel = "EMPTY",
        ) {
            PlaceholderCard(
                title = "No completed imports",
                body = "Completed jobs will list here in MVP 3.",
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
