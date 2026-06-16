package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.heathen.ialemus.core.spotify.SpotifyConnectionStatus
import com.heathen.ialemus.core.spotify.SpotifyViewModel
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

@Composable
fun StreamingScreen(
    spotifyViewModel: SpotifyViewModel,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val uiState by spotifyViewModel.uiState.collectAsStateWithLifecycle()
    val connected = uiState.connectionStatus == SpotifyConnectionStatus.CONNECTED
    val authInProgress = uiState.connectionStatus == SpotifyConnectionStatus.AUTH_IN_PROGRESS
    var accountExpanded by rememberSaveable { mutableStateOf(true) }

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
            subtitle = "External Spotify — separate from LOCAL CORE",
        )

        HudCollapsiblePanel(
            title = "Spotify Account",
            sectionTag = "ACCOUNT LINK",
            subtitle = when {
                authInProgress -> "Waiting for Spotify authorization…"
                connected -> uiState.profile?.displayName ?: "Connected"
                else -> "Connect Spotify to link your account"
            },
            expanded = accountExpanded,
            onToggle = { accountExpanded = !accountExpanded },
            statusLabel = uiState.connectionStatus.label.uppercase(),
        ) {
            HudStatusChip(
                label = uiState.connectionStatus.label.uppercase(),
                highlighted = connected,
                warning = uiState.connectionStatus == SpotifyConnectionStatus.ERROR ||
                    uiState.sessionExpired,
            )
            uiState.profile?.let { profile ->
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                profile.email?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                }
                profile.product?.let {
                    Text(text = "Account: $it", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                }
            }
            uiState.errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.warningColor, modifier = Modifier.padding(top = 6.dp))
            }
            if (!connected) {
                HudButton(
                    label = if (authInProgress) "Waiting for authorization…" else "Connect Spotify",
                    onClick = { spotifyViewModel.startLogin(context) },
                    enabled = !authInProgress,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudButton(label = "Refresh", onClick = spotifyViewModel::refreshPlayback, modifier = Modifier.weight(1f), accent = HudButtonAccent.Neutral)
                    HudButton(label = "Logout", onClick = spotifyViewModel::logout, modifier = Modifier.weight(1f), accent = HudButtonAccent.Neutral)
                }
            }
            HudButton(
                label = "Open Spotify App",
                onClick = { openSpotifyApp(context) },
                modifier = Modifier.padding(top = 8.dp),
                accent = HudButtonAccent.Neutral,
            )
        }

        HudPanel(title = "Spotify Remote Playback", sectionTag = "SPOTIFY REMOTE") {
            when {
                !connected -> Text("Connect Spotify to see remote playback.", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                uiState.noActiveDevice -> {
                    Text(
                        text = "No active Spotify device. Open Spotify and start playback on this device or another Spotify Connect device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                }
                uiState.playback != null -> {
                    val playback = uiState.playback!!
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        playback.albumArtUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Album art",
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column {
                            Text(playback.trackName, style = MaterialTheme.typography.bodyMedium, color = tokens.textPrimary)
                            Text(playback.artistName, style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                            Text(playback.albumName, style = MaterialTheme.typography.labelSmall, color = tokens.textMuted)
                            playback.deviceName?.let {
                                Text("Device: $it", style = MaterialTheme.typography.labelSmall, color = tokens.accentActive)
                            }
                            HudStatusChip(
                                label = if (playback.isPlaying) "PLAYING" else "PAUSED",
                                highlighted = playback.isPlaying,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
            if (connected) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    HudIconButton(icon = Icons.Filled.Shuffle, contentDescription = "Spotify shuffle", onClick = { spotifyViewModel.remoteAction("shuffle") })
                    HudIconButton(icon = Icons.Filled.SkipPrevious, contentDescription = "Spotify previous", onClick = { spotifyViewModel.remoteAction("previous") })
                    HudIconButton(
                        icon = if (uiState.playback?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Spotify play pause",
                        onClick = { spotifyViewModel.remoteAction(if (uiState.playback?.isPlaying == true) "pause" else "play") },
                        highlighted = true,
                    )
                    HudIconButton(icon = Icons.Filled.SkipNext, contentDescription = "Spotify next", onClick = { spotifyViewModel.remoteAction("next") })
                    HudIconButton(icon = Icons.Filled.Repeat, contentDescription = "Spotify repeat", onClick = { spotifyViewModel.remoteAction("repeat") }, enabled = false)
                }
                Text(
                    text = "SPOTIFY REMOTE controls — not LOCAL CORE. Requires active Spotify device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        HudPanel(title = "Spotify Library", sectionTag = "PLAYLISTS", subtitle = "Coming soon") {
            HudButton(label = "Spotify playlists", onClick = { }, enabled = false)
            HudButton(label = "Search Spotify", onClick = { }, enabled = false, modifier = Modifier.padding(top = 8.dp), accent = HudButtonAccent.Neutral)
            Text(
                text = "Send Spotify playlist to future Bridge job — disabled until Bridge MVP 2.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        HudPanel(title = "Docker download tools", sectionTag = "DOWNLOADS", subtitle = "MeTube, slskd, NAS UI on Downloads tab.") {
            Text(
                text = "Open Downloads for NAS Docker web modules.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
            )
        }
    }
}
