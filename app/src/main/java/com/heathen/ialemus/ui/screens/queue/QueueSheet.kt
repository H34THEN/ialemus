package com.heathen.ialemus.ui.screens.queue

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.EmptyLibraryState
import com.heathen.ialemus.ui.components.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit,
) {
    val queueItems by playerViewModel.queueItems.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Text(
            text = "Queue",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (queueItems.isEmpty()) {
            EmptyLibraryState(
                title = "Queue empty",
                body = "Play a track from Library to populate the queue.",
                modifier = Modifier.padding(16.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
            ) {
                items(queueItems, key = { it.track.id }) { item ->
                    TrackRow(
                        track = item.track,
                        onClick = { playerViewModel.playQueueItem(item.queueIndex) },
                    )
                }
            }
        }
        // TODO(MVP 1B): remove/reorder queue items.
    }
}
