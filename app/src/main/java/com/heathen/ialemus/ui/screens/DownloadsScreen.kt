package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Text(
            text = "Downloads",
            style = MaterialTheme.typography.headlineMedium,
        )

        PlaceholderCard(
            title = "Job queue",
            body = "Active bridge jobs (GET /jobs) will appear here in MVP 3.",
        )

        PlaceholderCard(
            title = "Completed imports",
            body = "Finished spot-dl, MeTube, and slskd downloads will list here after bridge folder watch + rescan.",
        )

        PlaceholderCard(
            title = "Failed jobs",
            body = "No failed jobs.",
        )

        // TODO: Landscape list + detail pane (MVP 5).
    }
}
