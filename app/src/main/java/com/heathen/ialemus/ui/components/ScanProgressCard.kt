package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.library.LibraryScanState

@Composable
fun ScanProgressCard(
    scanState: LibraryScanState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            when (scanState) {
                LibraryScanState.Idle -> {
                    Text("Ready to scan local music.")
                }
                LibraryScanState.Scanning -> {
                    CircularProgressIndicator()
                    Text(
                        text = "Scanning local music…",
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                is LibraryScanState.Complete -> {
                    Text("Scan complete: ${scanState.trackCount} tracks indexed.")
                }
                is LibraryScanState.Failed -> {
                    Text(
                        text = "Scan failed: ${scanState.message}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
