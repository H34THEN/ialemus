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
import androidx.compose.runtime.LaunchedEffect
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
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.core.settings.NasUrlPlaceholders
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.ServiceConnectionCard
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser

@Composable
fun AcquireScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val nasSettings by settingsViewModel.nasConnectionSettings.collectAsStateWithLifecycle()
    val meTubeStatus by settingsViewModel.meTubeTestStatus.collectAsStateWithLifecycle()
    val slskdStatus by settingsViewModel.slskdTestStatus.collectAsStateWithLifecycle()
    val bridgeStatus by settingsViewModel.bridgeTestStatus.collectAsStateWithLifecycle()

    var archNoteExpanded by rememberSaveable { mutableStateOf(false) }
    var meTubeUrl by rememberSaveable(nasSettings.meTubeUrl) { mutableStateOf(nasSettings.meTubeUrl) }
    var slskdUrl by rememberSaveable(nasSettings.slskdUrl) { mutableStateOf(nasSettings.slskdUrl) }

    var spotifyUrl by rememberSaveable { mutableStateOf("") }
    var playlistName by rememberSaveable { mutableStateOf("") }
    var spotDlFormat by rememberSaveable { mutableStateOf("m4a") }
    var outputTarget by rememberSaveable { mutableStateOf("nas_music_playlists") }
    var generateM3u by rememberSaveable { mutableStateOf(true) }
    var skipExisting by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(nasSettings.meTubeUrl) { meTubeUrl = nasSettings.meTubeUrl }
    LaunchedEffect(nasSettings.slskdUrl) { slskdUrl = nasSettings.slskdUrl }

    val bridgeReady = nasSettings.bridgeConfigured
    val spotDlStatus = when {
        !bridgeReady -> "Bridge required"
        bridgeStatus == ConnectionTestStatus.REACHABLE -> "Ready"
        bridgeStatus == ConnectionTestStatus.READY -> "Ready"
        else -> "Not connected"
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
            statusLabel = "NAS CONNECTORS",
            subtitle = "Configure LAN services · submit jobs via Bridge only",
        )

        HudCollapsiblePanel(
            title = "Bridge architecture",
            sectionTag = "NAS BRIDGE",
            subtitle = BridgePlaceholder.ARCHITECTURE_NOTE,
            expanded = archNoteExpanded,
            onToggle = { archNoteExpanded = !archNoteExpanded },
            statusLabel = if (bridgeReady) "CONFIGURED" else "REQUIRED",
        ) {
            Text(
                text = BridgePlaceholder.ARCHITECTURE_NOTE,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
            Text(
                text = "Android never runs shell, SSH, or Docker. spotDL, MeTube, and slskd jobs go through Ialemus Bridge on the NAS.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        ServiceConnectionCard(
            title = "MeTube",
            sectionTag = "MODULE 02",
            subtitle = "Connect to MeTube LAN web UI (e.g. port 38245 → 8081).",
            url = meTubeUrl,
            onUrlChange = { meTubeUrl = it },
            urlPlaceholder = NasUrlPlaceholders.METUBE,
            status = meTubeStatus,
            onOpenWebUi = { openUrlInBrowser(context, meTubeUrl) },
            onTestConnection = settingsViewModel::testMeTubeConnection,
            onSaveUrl = {
                settingsViewModel.saveServiceUrl(nasSettings, meTubeUrl = meTubeUrl.trim())
            },
            futureActionLabel = "Submit URL via Bridge (TODO)",
        )

        ServiceConnectionCard(
            title = "Soulseek / slskd",
            sectionTag = "MODULE 03",
            subtitle = "Connect to slskd LAN web UI (e.g. port 5031 → 5030).",
            url = slskdUrl,
            onUrlChange = { slskdUrl = it },
            urlPlaceholder = NasUrlPlaceholders.SLSKD,
            status = slskdStatus,
            onOpenWebUi = { openUrlInBrowser(context, slskdUrl) },
            onTestConnection = settingsViewModel::testSlskdConnection,
            onSaveUrl = {
                settingsViewModel.saveServiceUrl(nasSettings, slskdUrl = slskdUrl.trim())
            },
            futureActionLabel = "Search via Bridge (TODO)",
        )

        HudPanel(
            title = "spotDL",
            sectionTag = "MODULE 01",
            subtitle = "Playlist jobs via Ialemus Bridge — not executed on Android.",
        ) {
            OutlinedTextField(
                value = spotifyUrl,
                onValueChange = { spotifyUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Spotify playlist/album/track URL") },
                placeholder = { Text("https://open.spotify.com/playlist/...") },
                singleLine = true,
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
            )
            FormatSelector(
                selected = spotDlFormat,
                onSelect = { spotDlFormat = it },
                options = listOf("m4a", "mp3", "opus", "flac", "nas_default"),
            )
            OutputTargetSelector(
                selected = outputTarget,
                onSelect = { outputTarget = it },
            )
            RowSwitch(
                label = "Generate M3U/M3U8",
                checked = generateM3u,
                onCheckedChange = { generateM3u = it },
            )
            RowSwitch(
                label = "Skip existing",
                checked = skipExisting,
                onCheckedChange = { skipExisting = it },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HudStatusChip(
                    label = spotDlStatus.uppercase(),
                    highlighted = bridgeReady,
                    disabled = !bridgeReady,
                )
            }
            HudButton(
                label = "Submit to Bridge",
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "POST /jobs/spotdl/playlist on Ialemus Bridge (MVP 2+). Bridge runs allowlisted spotDL profiles on the NAS, likely inside the Gluetun-protected stack.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun FormatSelector(
    selected: String,
    onSelect: (String) -> Unit,
    options: List<String>,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "FORMAT",
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.accentActive,
        )
        Column(
            modifier = Modifier.padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { option ->
                        SelectChip(
                            label = option,
                            selected = selected == option,
                            onClick = { onSelect(option) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - row.size) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun OutputTargetSelector(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val options = listOf(
        "nas_music" to "NAS Music",
        "nas_music_playlists" to "NAS Playlists",
        "spotdl_staging" to "spotDL Staging",
    )
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "OUTPUT TARGET",
            style = MaterialTheme.typography.labelSmall,
            color = LocalIalemusTokens.current.accentActive,
        )
        Column(
            modifier = Modifier.padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { (value, label) ->
                SelectChip(
                    label = label,
                    selected = selected == value,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HudButton(
        label = label,
        onClick = onClick,
        enabled = true,
        modifier = modifier,
        accent = if (selected) HudButtonAccent.Primary else HudButtonAccent.Neutral,
    )
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
