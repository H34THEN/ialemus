package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyLibraryState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    sectionTag: String = "LOCAL SIGNAL",
    actions: @Composable (() -> Unit)? = null,
) {
    HudPanel(
        title = title,
        sectionTag = sectionTag,
        subtitle = body,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HudStatusChip(label = "STANDBY", disabled = true)
            actions?.invoke()
        }
    }
}

@Composable
fun PlaceholderCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    sectionTag: String? = "FUTURE MODULE",
    disabled: Boolean = true,
    content: @Composable (() -> Unit)? = null,
) {
    HudPanel(
        title = title,
        sectionTag = sectionTag,
        subtitle = body,
        modifier = modifier,
    ) {
        HudStatusChip(
            label = if (disabled) "DISABLED" else "ACTIVE",
            disabled = disabled,
            warning = !disabled,
        )
        content?.invoke()
    }
}
