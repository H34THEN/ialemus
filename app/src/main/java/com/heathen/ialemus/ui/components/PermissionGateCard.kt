package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.library.MediaPermissionState

@Composable
fun PermissionGateCard(
    permissionState: MediaPermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = when (permissionState) {
                    MediaPermissionState.Granted -> "Music access granted"
                    MediaPermissionState.NotGranted -> "Music access required"
                    MediaPermissionState.Denied -> "Music access denied"
                    MediaPermissionState.DeniedPermanently -> "Music access blocked"
                    MediaPermissionState.Unknown -> "Music access unknown"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = when (permissionState) {
                    MediaPermissionState.Granted ->
                        "You can scan local music from device storage."
                    MediaPermissionState.DeniedPermanently ->
                        "Open system settings to grant music access for Ialemus."
                    else ->
                        "Grant access to scan and play local audio files on this device."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
            when (permissionState) {
                MediaPermissionState.NotGranted,
                MediaPermissionState.Denied,
                -> {
                    Button(onClick = onRequestPermission) {
                        Text("Grant music access")
                    }
                }
                MediaPermissionState.DeniedPermanently -> {
                    OutlinedButton(onClick = onOpenSettings) {
                        Text("Open settings")
                    }
                }
                else -> Unit
            }
        }
    }
}
