package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.network.BridgePlaceholder
import com.heathen.ialemus.ui.components.PlaceholderCard

@Composable
fun AcquireScreen(modifier: Modifier = Modifier) {
    var spotDlInput by rememberSaveable { mutableStateOf("") }
    var meTubeInput by rememberSaveable { mutableStateOf("") }
    var slskdInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Acquire",
            style = MaterialTheme.typography.headlineMedium,
        )

        PlaceholderCard(
            title = "Architecture note",
            body = BridgePlaceholder.ARCHITECTURE_NOTE,
        )

        OutlinedTextField(
            value = spotDlInput,
            onValueChange = { spotDlInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("spot-dl — Spotify URL (placeholder)") },
            placeholder = { Text("https://open.spotify.com/playlist/...") },
            singleLine = true,
            readOnly = true,
        )

        OutlinedTextField(
            value = meTubeInput,
            onValueChange = { meTubeInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("MeTube — video URL (placeholder)") },
            placeholder = { Text("https://...") },
            singleLine = true,
            readOnly = true,
        )

        OutlinedTextField(
            value = slskdInput,
            onValueChange = { slskdInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("slskd / Soulseek — search (placeholder)") },
            placeholder = { Text("artist - track") },
            singleLine = true,
            readOnly = true,
        )

        PlaceholderCard(
            title = "Submit disabled",
            body = "Job submission via POST /jobs on Ialemus Bridge arrives in MVP 3–4. No shell/SSH/Docker from Android.",
        )

        // TODO: Landscape two-pane acquire layout (MVP 5).
    }
}
