package com.heathen.ialemus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.components.PlaceholderCard

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineMedium,
        )

        PlaceholderCard(
            title = "Local library scan",
            body = "MediaStore and Storage Access Framework scanning will be implemented in MVP 1.",
        )

        PlaceholderCard(
            title = "Tracks",
            body = "No tracks indexed yet.",
        )

        PlaceholderCard(
            title = "Albums",
            body = "No albums indexed yet.",
        )

        PlaceholderCard(
            title = "Artists",
            body = "No artists indexed yet.",
        )

        // TODO: Landscape master-detail library layout (MVP 5).
    }
}
