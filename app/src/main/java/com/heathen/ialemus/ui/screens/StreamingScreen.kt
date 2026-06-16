package com.heathen.ialemus.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.MaterialTheme
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
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.settings.SpotifyConnectionStatus
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudHeader
import com.heathen.ialemus.ui.components.HudIconButton
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openSpotifyApp
import com.heathen.ialemus.ui.util.openUrlInBrowser

@Composable
fun StreamingScreen(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val spotifySettings by settingsViewModel.spotifySettings.collectAsStateWithLifecycle()
    val spotifyStatus by settingsViewModel.spotifyConnectionStatus.collectAsStateWithLifecycle()

    var accountExpanded by rememberSaveable { mutableStateOf(true) }
    var playbackExpanded by rememberSaveable { mutableStateOf(true) }
    var libraryExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HudHeader(
            title = "Streaming",
            statusLabel = "SPOTIFY REMOTE",
            subtitle = "External Spotify playback — separate from LOCAL CORE",
        )

        HudCollapsiblePanel(
            title = "Spotify Account",
            sectionTag = "ACCOUNT LINK",
            subtitle = "Configure Client ID in Settings, then login with PKCE (scaffold).",
            expanded = accountExpanded,
            onToggle = { accountExpanded = !accountExpanded },
            statusLabel = spotifyStatus.label.uppercase(),
        ) {
            HudStatusChip(
                label = spotifyStatus.label.uppercase(),
                highlighted = spotifyStatus == SpotifyConnectionStatus.CONNECTED,
                disabled = spotifyStatus == SpotifyConnectionStatus.NOT_CONFIGURED,
            )
            if (spotifySettings.displayName.isNotBlank()) {
                Text(
                    text = "Account: ${spotifySettings.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textPrimary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Text(
                text = "Playback source: SPOTIFY REMOTE — controls the Spotify app / Connect, not Ialemus ExoPlayer.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HudButton(
                    label = "Login with Spotify",
                    onClick = { settingsViewModel.loginWithSpotifyScaffold(context) },
                    enabled = spotifySettings.configured,
                    modifier = Modifier.weight(1f),
                )
                HudButton(
                    label = "Logout",
                    onClick = settingsViewModel::logoutSpotify,
                    enabled = spotifySettings.connected,
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Neutral,
                )
            }
            HudButton(
                label = "Open Spotify App",
                onClick = { openSpotifyApp(context) },
                modifier = Modifier.padding(top = 8.dp),
                accent = HudButtonAccent.Neutral,
            )
            Text(
                text = "Configure Client ID and Redirect URI in Settings → Spotify Integration.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        HudCollapsiblePanel(
            title = "Spotify Playback",
            sectionTag = "REMOTE CONTROL",
            subtitle = "App Remote scaffold — play/pause via Spotify app when connected.",
            expanded = playbackExpanded,
            onToggle = { playbackExpanded = !playbackExpanded },
            statusLabel = if (spotifySettings.connected) "LINKED" else "STANDBY",
        ) {
            HudPanel(title = "Now Playing", sectionTag = "SPOTIFY REMOTE") {
                Text(
                    text = if (spotifySettings.connected) {
                        "Spotify track metadata will appear here after App Remote connects."
                    } else {
                        "Not connected — login or open Spotify app."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textMuted,
                )
                Text(
                    text = "Artist · Album · Track title (TODO)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HudIconButton(
                    icon = Icons.Filled.Shuffle,
                    contentDescription = "Spotify shuffle",
                    onClick = { settingsViewModel.spotifyRemoteAction("shuffle") },
                    enabled = spotifySettings.connected,
                )
                HudIconButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = "Spotify previous",
                    onClick = { settingsViewModel.spotifyRemoteAction("previous") },
                    enabled = spotifySettings.connected,
                )
                HudIconButton(
                    icon = Icons.Filled.PlayArrow,
                    contentDescription = "Spotify play",
                    onClick = { settingsViewModel.spotifyRemoteAction("play") },
                    enabled = spotifySettings.connected,
                    highlighted = true,
                )
                HudIconButton(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = "Spotify next",
                    onClick = { settingsViewModel.spotifyRemoteAction("next") },
                    enabled = spotifySettings.connected,
                )
                HudIconButton(
                    icon = Icons.Filled.Repeat,
                    contentDescription = "Spotify repeat",
                    onClick = { settingsViewModel.spotifyRemoteAction("repeat") },
                    enabled = spotifySettings.connected,
                )
            }
            Text(
                text = "These controls target Spotify playback only. Local files use Now Playing → LOCAL CORE.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        HudCollapsiblePanel(
            title = "Spotify Library",
            sectionTag = "PLAYLISTS",
            subtitle = "Playlists, search, and spotDL bridge handoff (future).",
            expanded = libraryExpanded,
            onToggle = { libraryExpanded = !libraryExpanded },
            statusLabel = "TODO",
        ) {
            HudButton(label = "Spotify playlists", onClick = { }, enabled = false)
            HudButton(
                label = "Search Spotify",
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(top = 8.dp),
                accent = HudButtonAccent.Neutral,
            )
            Text(
                text = "Send playlist to future spotDL bridge — disabled until Bridge MVP 2.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        HudPanel(
            title = "Docker download tools",
            sectionTag = "DOWNLOADS",
            subtitle = "MeTube, slskd, NAS UI, and spotDL modules moved to Downloads.",
        ) {
            Text(
                text = "Open Downloads → MeTube / slskd / NAS / spotDL modules for NAS Docker web UIs.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
        }
    }
}
