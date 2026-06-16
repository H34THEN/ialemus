package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.PlaceholderCard

@Composable
fun DownloadsScreen(modifier: Modifier = Modifier) {
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

        PlaceholderCard(
            title = "Job queue",
            body = "Active bridge jobs (GET /jobs) will appear here in MVP 3.",
            sectionTag = "QUEUE SYNC",
        ) {
            HudStatusChip(label = "0 active jobs", disabled = true)
        }

        PlaceholderCard(
            title = "Completed imports",
            body = "Finished spot-dl, MeTube, and slskd downloads will list here after bridge folder watch + rescan.",
            sectionTag = "LOCAL SIGNAL",
        )

        PlaceholderCard(
            title = "Failed jobs",
            body = "No failed jobs.",
            sectionTag = "ALERT",
        )
    }
}
