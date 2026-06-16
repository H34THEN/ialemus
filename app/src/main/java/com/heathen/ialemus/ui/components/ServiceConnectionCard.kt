package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun ServiceConnectionCard(
    title: String,
    sectionTag: String,
    subtitle: String,
    url: String,
    onUrlChange: (String) -> Unit,
    urlPlaceholder: String,
    status: ConnectionTestStatus,
    onOpenWebUi: () -> Unit,
    onTestConnection: () -> Unit,
    onSaveUrl: () -> Unit,
    openWebUiEnabled: Boolean = url.isNotBlank(),
    testEnabled: Boolean = url.isNotBlank(),
    saveEnabled: Boolean = true,
    futureActionLabel: String,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val statusHighlighted = status == ConnectionTestStatus.REACHABLE || status == ConnectionTestStatus.READY

    HudPanel(
        title = title,
        sectionTag = sectionTag,
        subtitle = subtitle,
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Service URL") },
            placeholder = { Text(urlPlaceholder) },
            singleLine = true,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HudStatusChip(
                label = status.label.uppercase(),
                highlighted = statusHighlighted,
                disabled = status == ConnectionTestStatus.NOT_CONFIGURED,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HudButton(
                label = "Open Web UI",
                onClick = onOpenWebUi,
                enabled = openWebUiEnabled,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Neutral,
            )
            HudButton(
                label = "Test",
                onClick = onTestConnection,
                enabled = testEnabled && status != ConnectionTestStatus.CHECKING,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Primary,
            )
        }
        HudButton(
            label = "Save URL",
            onClick = onSaveUrl,
            enabled = saveEnabled,
            modifier = Modifier.padding(top = 8.dp),
        )
        HudButton(
            label = futureActionLabel,
            onClick = { },
            enabled = false,
            modifier = Modifier.padding(top = 8.dp),
            accent = HudButtonAccent.Neutral,
        )
        Text(
            text = "Opens the LAN web UI in your browser. Bridge API integration is TODO.",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
