package com.heathen.ialemus.ui.screens.nowplaying

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.lyrics.LyricsParser
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlaybackState
import com.heathen.ialemus.data.local.entity.LyricsEntity
import com.heathen.ialemus.data.local.entity.LyricsSourceType
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudOutlinedTextField
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun NowPlayingLyricsPanel(
    track: Track,
    playbackState: PlaybackState,
    lyrics: LyricsEntity?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSaveManual: (String) -> Unit,
    onClear: () -> Unit,
    onScanSidecar: () -> Unit,
    onTryEmbedded: () -> Unit,
    onImportFile: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    var editorText by remember(track.id, lyrics?.rawText) {
        mutableStateOf(lyrics?.rawText.orEmpty())
    }
    var editing by remember(track.id) { mutableStateOf(lyrics == null) }
    val parsed = remember(lyrics?.rawText) {
        lyrics?.rawText?.let { LyricsParser.parse(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(onImportFile)
    }

    val statusLabel = when (lyrics?.sourceType) {
        LyricsSourceType.MANUAL.name -> "MANUAL"
        LyricsSourceType.LRC_SIDECAR.name -> "LRC"
        LyricsSourceType.TXT_SIDECAR.name -> "TXT"
        LyricsSourceType.EMBEDDED.name -> "EMBED"
        LyricsSourceType.FUTURE_PROVIDER.name -> "API"
        null -> "NONE"
        else -> "LYRICS"
    }

    HudCollapsiblePanel(
        title = "Lyrics",
        sectionTag = "LYRICS",
        subtitle = lyricsSubtitle(lyrics, parsed?.isSynced == true),
        expanded = expanded,
        onToggle = onToggle,
        statusLabel = statusLabel,
        modifier = modifier,
    ) {
        if (editing || lyrics == null) {
            HudOutlinedTextField(
                value = editorText,
                onValueChange = { editorText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp),
                label = "Paste or edit lyrics (LRC timestamps supported)",
                singleLine = false,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HudButton(label = "Save lyrics", onClick = {
                    onSaveManual(editorText)
                    editing = false
                })
                if (lyrics != null) {
                    HudButton(
                        label = "Cancel edit",
                        onClick = {
                            editorText = lyrics.rawText
                            editing = false
                        },
                        accent = HudButtonAccent.Neutral,
                    )
                }
            }
        } else if (parsed != null && parsed.isSynced) {
            SyncedLyricsList(
                lines = parsed.lines,
                positionMs = playbackState.positionMs,
            )
            HudButton(
                label = "Edit lyrics",
                onClick = { editing = true },
                accent = HudButtonAccent.Neutral,
            )
        } else if (parsed != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
            ) {
                item {
                    Text(
                        text = parsed.plainText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textPrimary,
                    )
                }
            }
            HudButton(
                label = "Edit lyrics",
                onClick = { editing = true },
                accent = HudButtonAccent.Neutral,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            HudButton(
                label = "Paste / edit lyrics",
                onClick = { editing = true },
                accent = HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Import .lrc or .txt file",
                onClick = { importLauncher.launch(arrayOf("text/*", "application/octet-stream")) },
                accent = HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Scan sidecar lyrics",
                onClick = onScanSidecar,
                accent = HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Try embedded lyrics",
                onClick = onTryEmbedded,
                accent = HudButtonAccent.Neutral,
            )
            if (lyrics != null) {
                HudButton(
                    label = "Clear lyrics",
                    onClick = onClear,
                    accent = HudButtonAccent.Warning,
                )
            }
        }
        Text(
            text = "Local/user-provided lyrics only. No web scraping or bundled copyrighted lyrics.",
            style = MaterialTheme.typography.labelSmall,
            color = tokens.textMuted,
        )
    }
}

@Composable
private fun SyncedLyricsList(
    lines: List<com.heathen.ialemus.core.lyrics.LyricLine>,
    positionMs: Long,
) {
    val tokens = LocalIalemusTokens.current
    val activeIndex = remember(lines, positionMs) {
        LyricsParser.currentLineIndex(lines, positionMs)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex.coerceAtLeast(0))
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(lines) { index, line ->
            val active = index == activeIndex
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (active) tokens.accentActive else tokens.textMuted,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun lyricsSubtitle(lyrics: LyricsEntity?, synced: Boolean): String = when {
    lyrics == null -> "Paste, import, or scan local lyrics."
    synced -> "Synced LRC — highlights follow playback."
    else -> "Unsynced lyrics."
}
