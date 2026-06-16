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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.heathen.ialemus.core.spotify.PlaybackSource
import com.heathen.ialemus.core.spotify.SpotifyAppStatus
import com.heathen.ialemus.core.spotify.SpotifyConnectionStatus
import com.heathen.ialemus.core.spotify.SpotifyRemoteConnectionState
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
import com.heathen.ialemus.ui.util.openSpotifyWeb

/** Generic Spotify editorial playlist for optional App Remote test play. */
private const val SPOTIFY_TEST_PLAYLIST_URI = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M"

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
    val remoteConnected = uiState.remoteConnectionState == SpotifyRemoteConnectionState.CONNECTED
    val remoteConnecting = uiState.remoteConnectionState == SpotifyRemoteConnectionState.CONNECTING
    val appInstalled = uiState.spotifyAppStatus == SpotifyAppStatus.INSTALLED
    val controlsEnabled = remoteConnected ||
        (connected && !uiState.noActiveDevice && uiState.playback != null)
    var accountExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        spotifyViewModel.refreshSpotifyAppStatus(context)
    }

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
            subtitle = "Spotify plays in the Spotify app — Ialemus controls it remotely",
        )

        Text(
            text = "LOCAL CORE: local files loaded by Ialemus · SPOTIFY REMOTE: Spotify app / Connect playback",
            style = MaterialTheme.typography.labelSmall,
            color = tokens.textMuted,
        )

        HudPanel(
            title = "Spotify on this HiBy R4",
            sectionTag = "DEVICE ACTIVATION",
            subtitle = when {
                uiState.spotifyAppStatus == SpotifyAppStatus.NOT_INSTALLED -> "Spotify app not installed"
                remoteConnected -> "HiBy R4 Spotify Remote connected"
                appInstalled -> "Spotify app installed"
                else -> uiState.spotifyAppStatus.label
            },
        ) {
            HudStatusChip(
                label = uiState.spotifyAppStatus.label.uppercase(),
                highlighted = appInstalled,
                warning = uiState.spotifyAppStatus == SpotifyAppStatus.NOT_INSTALLED,
            )
            HudStatusChip(
                label = "REMOTE: ${uiState.remoteConnectionState.label.uppercase()}",
                highlighted = remoteConnected,
                warning = uiState.remoteConnectionState == SpotifyRemoteConnectionState.ERROR,
                modifier = Modifier.padding(top = 4.dp),
            )

            when {
                uiState.spotifyAppStatus == SpotifyAppStatus.NOT_INSTALLED -> {
                    Text(
                        text = "Install or sideload Spotify on the HiBy R4, then sign in. Ialemus controls Spotify through the Spotify app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HudButton(
                            label = "Open Spotify Web",
                            onClick = { openSpotifyWeb(context) },
                            modifier = Modifier.weight(1f),
                        )
                        HudButton(
                            label = "Recheck",
                            onClick = { spotifyViewModel.refreshSpotifyAppStatus(context) },
                            modifier = Modifier.weight(1f),
                            accent = HudButtonAccent.Neutral,
                        )
                    }
                }

                remoteConnected -> {
                    Text(
                        text = "HiBy R4 Spotify Remote connected. Playback stays in the Spotify app; Ialemus sends remote commands.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textPrimary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    uiState.remoteErrorMessage?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.warningColor, modifier = Modifier.padding(top = 4.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HudButton(label = "Open Spotify App", onClick = { openSpotifyApp(context) }, modifier = Modifier.weight(1f), accent = HudButtonAccent.Neutral)
                        HudButton(label = "Refresh Playback", onClick = spotifyViewModel::refreshPlayback, modifier = Modifier.weight(1f), accent = HudButtonAccent.Neutral)
                    }
                }

                appInstalled -> {
                    Text(
                        text = "Open Spotify once on this HiBy R4 and start playback, then return to Ialemus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    uiState.remoteErrorMessage?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.warningColor, modifier = Modifier.padding(top = 4.dp))
                    }
                    if (connected && uiState.noActiveDevice && uiState.devices.isEmpty()) {
                        Text(
                            text = "No active Spotify device. Open Spotify on this HiBy R4 and start any track, then return here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.warningColor,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HudButton(label = "Open Spotify App", onClick = { openSpotifyApp(context) }, modifier = Modifier.weight(1f))
                        HudButton(
                            label = if (remoteConnecting) "Connecting…" else "Connect Spotify Remote",
                            onClick = { spotifyViewModel.connectSpotifyRemote(context) },
                            enabled = !remoteConnecting,
                            modifier = Modifier.weight(1f),
                            accent = HudButtonAccent.Neutral,
                        )
                    }
                    if (connected) {
                        HudButton(
                            label = if (uiState.devicesLoading) "Refreshing devices…" else "Refresh Devices",
                            onClick = spotifyViewModel::refreshDevices,
                            enabled = !uiState.devicesLoading,
                            modifier = Modifier.padding(top = 8.dp),
                            accent = HudButtonAccent.Neutral,
                        )
                    }
                    uiState.devicesError?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = tokens.warningColor, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                else -> {
                    Text(
                        text = "Checking Spotify app status…",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    HudButton(
                        label = "Recheck",
                        onClick = { spotifyViewModel.refreshSpotifyAppStatus(context) },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            if (connected && uiState.devices.isNotEmpty()) {
                Text(
                    text = "SPOTIFY CONNECT DEVICES",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.accentActive,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
                uiState.devices.forEach { device ->
                    val likelyLocal = device.name.contains("hiby", ignoreCase = true) ||
                        device.name.contains("r4", ignoreCase = true) ||
                        device.type.equals("Smartphone", ignoreCase = true)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.name, style = MaterialTheme.typography.bodyMedium, color = tokens.textPrimary)
                                Text(
                                    text = buildString {
                                        append(device.type)
                                        if (device.isActive) append(" · ACTIVE")
                                        device.volumePercent?.let { append(" · vol $it%") }
                                        if (device.isRestricted) append(" · restricted")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.textMuted,
                                )
                            }
                            if (likelyLocal) {
                                HudStatusChip(label = "LIKELY LOCAL", highlighted = true)
                            }
                        }
                        if (!device.isActive) {
                            HudButton(
                                label = "Transfer playback",
                                onClick = { spotifyViewModel.transferToDevice(device.id) },
                                modifier = Modifier.padding(top = 4.dp),
                                accent = HudButtonAccent.Neutral,
                            )
                        }
                    }
                }
            }
        }

        HudCollapsiblePanel(
            title = "Spotify Account",
            sectionTag = "ACCOUNT LINK",
            subtitle = when {
                authInProgress -> "Waiting for Spotify authorization…"
                connected -> uiState.profile?.displayName ?: "Connected"
                else -> "Connect Spotify to list Connect devices via Web API"
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
        }

        HudPanel(title = "Spotify Remote Playback", sectionTag = "SPOTIFY REMOTE") {
            when {
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
                            Text(
                                text = if (playback.source == PlaybackSource.APP_REMOTE) "Source: App Remote" else "Source: Web API",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.textMuted,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
                connected && uiState.noActiveDevice && !remoteConnected -> {
                    Text(
                        text = "No active Spotify device. Open Spotify on this HiBy R4 and start playback, then return to Ialemus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                }
                !connected && !remoteConnected -> {
                    Text(
                        text = "Connect Spotify account or Spotify Remote to see playback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                }
                else -> {
                    Text(
                        text = "Waiting for Spotify playback state…",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                HudIconButton(
                    icon = Icons.Filled.Shuffle,
                    contentDescription = "Spotify shuffle",
                    onClick = { spotifyViewModel.remoteAction("shuffle") },
                    enabled = controlsEnabled,
                )
                HudIconButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = "Spotify previous",
                    onClick = { spotifyViewModel.remoteAction("previous") },
                    enabled = controlsEnabled,
                )
                HudIconButton(
                    icon = if (uiState.playback?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Spotify play pause",
                    onClick = { spotifyViewModel.remoteAction(if (uiState.playback?.isPlaying == true) "pause" else "play") },
                    highlighted = true,
                    enabled = controlsEnabled,
                )
                HudIconButton(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = "Spotify next",
                    onClick = { spotifyViewModel.remoteAction("next") },
                    enabled = controlsEnabled,
                )
                HudIconButton(
                    icon = Icons.Filled.Repeat,
                    contentDescription = "Spotify repeat",
                    onClick = { spotifyViewModel.remoteAction("repeat") },
                    enabled = controlsEnabled && !remoteConnected,
                )
                HudIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Refresh Spotify playback",
                    onClick = spotifyViewModel::refreshPlayback,
                )
            }
            Text(
                text = if (controlsEnabled) {
                    "SPOTIFY REMOTE controls — not LOCAL CORE."
                } else {
                    "Open Spotify App to activate a Spotify device."
                },
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
            HudButton(
                label = "Play Spotify test playlist",
                onClick = { spotifyViewModel.playTestUri(SPOTIFY_TEST_PLAYLIST_URI) },
                enabled = remoteConnected,
                modifier = Modifier.padding(top = 8.dp),
                accent = HudButtonAccent.Neutral,
            )
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
