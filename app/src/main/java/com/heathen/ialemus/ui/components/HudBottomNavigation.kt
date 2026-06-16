package com.heathen.ialemus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.navigation.AppDestination
import com.heathen.ialemus.ui.theme.CompactLayout
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudBottomNavigation(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(tokens.surfaceDeep.copy(alpha = 0.95f))
            .border(1.dp, tokens.hudBorderColor.copy(alpha = 0.5f)),
    ) {
        Text(
            text = selected.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = tokens.accentActive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            maxLines = 1,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDestination.entries.forEach { destination ->
                HudNavItem(
                    destination = destination,
                    selected = selected == destination,
                    onClick = { onSelect(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HudNavItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = if (selected) tokens.navActive else tokens.hudBorderColor.copy(alpha = 0.35f)
    val iconTint = if (selected) tokens.navActive else tokens.textMuted
    val backgroundColor = if (selected) tokens.panelOverlay else tokens.surfaceDeep.copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small,
            )
            .background(backgroundColor, MaterialTheme.shapes.small)
            .padding(
                horizontal = CompactLayout.dockItemPadding,
                vertical = CompactLayout.dockItemPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = iconTint,
            modifier = Modifier.size(CompactLayout.dockIconSize),
        )
    }
}
