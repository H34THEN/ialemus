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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.navigation.AppDestination
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
            text = "COMMAND DOCK · DAP MODE",
            style = MaterialTheme.typography.labelSmall,
            color = tokens.textMuted,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AppDestination.entries.forEach { destination ->
                HudNavItem(
                    destination = destination,
                    selected = selected == destination,
                    onClick = { onSelect(destination) },
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
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = if (selected) tokens.navActive else tokens.hudBorderColor.copy(alpha = 0.35f)
    val iconTint = if (selected) tokens.navActive else tokens.textMuted
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small,
            )
            .background(
                if (selected) tokens.panelOverlay else tokens.surfaceDeep.copy(alpha = 0.6f),
                MaterialTheme.shapes.small,
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = destination.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = iconTint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
