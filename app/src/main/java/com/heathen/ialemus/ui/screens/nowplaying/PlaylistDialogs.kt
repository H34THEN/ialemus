package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.playlist.PlaylistSummary
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudOutlinedTextField
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistSummary>,
    onDismiss: () -> Unit,
    onCreateAndAdd: (String) -> Unit,
    onAddToExisting: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(playlists.isEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showCreate || playlists.isEmpty()) {
                    HudOutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = "New playlist name",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HudButton(
                        label = "Create & Add",
                        onClick = { onCreateAndAdd(newName.ifBlank { "New Playlist" }) },
                        enabled = newName.isNotBlank() || playlists.isEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (playlists.isNotEmpty()) {
                        TextButton(onClick = { showCreate = false }) {
                            Text("Pick existing playlist")
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(playlists, key = { it.id }) { playlist ->
                            HudButton(
                                label = "${playlist.name} (${playlist.trackCount})",
                                onClick = { onAddToExisting(playlist.id) },
                                modifier = Modifier.fillMaxWidth(),
                                accent = HudButtonAccent.Neutral,
                            )
                        }
                    }
                    TextButton(onClick = { showCreate = true }) {
                        Text("Create new playlist")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            HudOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Playlist name",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.ifBlank { "New Playlist" }) },
                enabled = name.isNotBlank(),
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun ImportResultBanner(
    matched: Int,
    unmatched: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    Column(modifier = modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HudStatusChip(label = "IMPORTED · $matched matched", highlighted = matched > 0)
        if (unmatched > 0) {
            Text(
                text = "$unmatched entries could not be matched to local tracks.",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.warningColor,
            )
        }
        HudButton(label = "Dismiss", onClick = onDismiss, accent = HudButtonAccent.Neutral)
    }
}
