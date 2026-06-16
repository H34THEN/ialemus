package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HudScaffold(
    snackbarHostState: SnackbarHostState,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    HudBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = bottomBar,
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}
