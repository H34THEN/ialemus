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
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.PlaceholderCard
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun AcquireScreen(modifier: Modifier = Modifier) {
    val tokens = LocalIalemusTokens.current
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
        HudHeader(
            title = "Acquire",
            statusLabel = "FUTURE MODULES",
            subtitle = "Bridge acquisition console — MVP 3+",
        )

        PlaceholderCard(
            title = "Architecture note",
            body = BridgePlaceholder.ARCHITECTURE_NOTE,
            sectionTag = "NAS BRIDGE REQUIRED",
        )

        HudPanel(title = "spot-dl", sectionTag = "MODULE 01", subtitle = "Spotify URL import — disabled") {
            OutlinedTextField(
                value = spotDlInput,
                onValueChange = { spotDlInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Spotify URL") },
                placeholder = { Text("https://open.spotify.com/playlist/...") },
                singleLine = true,
                readOnly = true,
                enabled = false,
            )
            HudStatusChip(label = "spot-dl", disabled = true, modifier = Modifier.padding(top = 8.dp))
        }

        HudPanel(title = "MeTube", sectionTag = "MODULE 02", subtitle = "Video URL import — disabled") {
            OutlinedTextField(
                value = meTubeInput,
                onValueChange = { meTubeInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Video URL") },
                placeholder = { Text("https://...") },
                singleLine = true,
                readOnly = true,
                enabled = false,
            )
            HudStatusChip(label = "MeTube", disabled = true, modifier = Modifier.padding(top = 8.dp))
        }

        HudPanel(title = "Soulseek / slskd", sectionTag = "MODULE 03", subtitle = "P2P search — disabled") {
            OutlinedTextField(
                value = slskdInput,
                onValueChange = { slskdInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search query") },
                placeholder = { Text("artist - track") },
                singleLine = true,
                readOnly = true,
                enabled = false,
            )
            HudStatusChip(label = "slskd", disabled = true, modifier = Modifier.padding(top = 8.dp))
        }

        PlaceholderCard(
            title = "Submit disabled",
            body = "Job submission via POST /jobs on Ialemus Bridge arrives in MVP 3–4. No shell/SSH/Docker from Android.",
            sectionTag = "QUEUE SYNC",
        )

        Text(
            text = "All acquire modules require NAS Bridge (MVP 2).",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textMuted,
        )
    }
}
