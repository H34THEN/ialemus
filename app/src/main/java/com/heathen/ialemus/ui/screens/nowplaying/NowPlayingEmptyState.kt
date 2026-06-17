package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.heathen.ialemus.R
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.library.LibraryScanState
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun NowPlayingEmptyState(
    trackCount: Int,
    sourceCount: Int,
    scanState: LibraryScanState,
    lastPlayedTrack: Track?,
    onChooseFolder: () -> Unit,
    onSyncAll: () -> Unit,
    onSyncFolder: () -> Unit,
    onOpenLibrary: () -> Unit,
    onShuffleAll: () -> Unit,
    onResumeLast: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val isScanning = scanState is LibraryScanState.ScanningFolders ||
        scanState is LibraryScanState.ScanningFullDevice
    val hasTracks = trackCount > 0
    val hasSources = sourceCount > 0
    val title = when {
        !hasTracks && !hasSources -> "No music loaded yet"
        !hasTracks -> "No tracks indexed"
        else -> "Nothing playing"
    }
    val subtitle = when {
        !hasSources -> "Choose a music folder to index local audio."
        !hasTracks -> "Sync your folder sources to build the library."
        else -> "Pick a track from Library or resume playback below."
    }

    HudPanel(
        title = "Playback Launchpad",
        sectionTag = "AUDIO CORE",
        subtitle = subtitle,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .background(tokens.surfaceDeep.copy(alpha = 0.85f), MaterialTheme.shapes.medium)
                .border(1.5.dp, tokens.accentActive.copy(alpha = 0.45f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Ialemus",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(56.dp),
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.accentActive,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (hasTracks) "$trackCount tracks indexed" else "Awaiting local signal",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            HudStatusChip(label = "LIBRARY · $trackCount", highlighted = hasTracks)
            HudStatusChip(label = "SOURCES · $sourceCount", highlighted = hasSources)
            if (isScanning) HudStatusChip(label = "SYNCING", warning = true)
        }

        if (!hasSources) {
            HudButton(label = "Choose Music Folder", onClick = onChooseFolder, modifier = Modifier.fillMaxWidth())
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            HudButton(
                label = "Sync All",
                onClick = onSyncAll,
                enabled = hasSources && !isScanning,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Primary,
            )
            HudButton(
                label = "Sync Folder",
                onClick = onSyncFolder,
                enabled = hasSources && !isScanning,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Neutral,
            )
        }

        if (hasTracks) {
            lastPlayedTrack?.let { track ->
                HudButton(
                    label = "Resume · ${track.displayTitle.take(28)}",
                    onClick = { onResumeLast(track) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HudButton(
                    label = "Open Library",
                    onClick = onOpenLibrary,
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Neutral,
                )
                HudButton(
                    label = "Shuffle All",
                    onClick = onShuffleAll,
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Warning,
                )
            }
        } else if (hasSources) {
            HudButton(
                label = "Open Library",
                onClick = onOpenLibrary,
                modifier = Modifier.fillMaxWidth(),
                accent = HudButtonAccent.Neutral,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Icon(Icons.Filled.Sync, contentDescription = null, tint = tokens.textMuted, modifier = Modifier.size(14.dp))
            Text(
                text = "Sync actions also remain on Library via SOURCES.",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Filled.LibraryMusic, contentDescription = null, tint = tokens.textMuted, modifier = Modifier.size(14.dp))
            Text(
                text = "Library upper-right update option unchanged.",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
            )
        }
    }
}
