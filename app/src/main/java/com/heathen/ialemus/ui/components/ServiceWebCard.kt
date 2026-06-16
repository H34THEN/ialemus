package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.network.ConnectionTestStatus
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun ServiceWebCard(
    title: String,
    sectionTag: String,
    subtitle: String,
    savedUrl: String,
    urlPlaceholder: String,
    status: ConnectionTestStatus,
    onOpenInApp: () -> Unit,
    onOpenExternal: () -> Unit,
    onTestConnection: () -> Unit,
    onSaveUrl: (String) -> Unit,
    validationError: String? = null,
    modifier: Modifier = Modifier,
    futureActionLabel: String? = null,
) {
    val tokens = LocalIalemusTokens.current
    var editing by rememberSaveable { mutableStateOf(false) }
    var draftUrl by rememberSaveable(savedUrl) { mutableStateOf(savedUrl) }
    val displayUrl = savedUrl.ifBlank { urlPlaceholder }
    val statusHighlighted = status == ConnectionTestStatus.REACHABLE || status == ConnectionTestStatus.READY
    val canOpen = savedUrl.isNotBlank()

    HudPanel(
        title = title,
        sectionTag = sectionTag,
        subtitle = subtitle,
        modifier = modifier,
    ) {
        if (!editing) {
            Text(
                text = displayUrl,
                style = MaterialTheme.typography.bodySmall,
                color = if (canOpen) tokens.textPrimary else tokens.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            HudOutlinedTextField(
                value = draftUrl,
                onValueChange = { draftUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = "Service URL",
                placeholder = urlPlaceholder,
                isError = validationError != null,
                supportingText = validationError,
            )
        }

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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HudButton(
                label = "Open in Ialemus",
                onClick = onOpenInApp,
                enabled = canOpen,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Primary,
            )
            HudButton(
                label = "External",
                onClick = onOpenExternal,
                enabled = canOpen,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Neutral,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HudButton(
                label = "Test",
                onClick = onTestConnection,
                enabled = canOpen && status != ConnectionTestStatus.CHECKING,
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Neutral,
            )
            HudButton(
                label = if (editing) "Save URL" else "Edit URL",
                onClick = {
                    if (editing) {
                        onSaveUrl(draftUrl)
                        editing = false
                    } else {
                        draftUrl = savedUrl.ifBlank { urlPlaceholder }
                        editing = true
                    }
                },
                modifier = Modifier.weight(1f),
                accent = HudButtonAccent.Neutral,
            )
        }
        if (futureActionLabel != null) {
            HudButton(
                label = futureActionLabel,
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(top = 6.dp),
                accent = HudButtonAccent.Neutral,
            )
        }
        Text(
            text = "Loads the Docker web UI inside Ialemus. No shell, SSH, or credential storage.",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
