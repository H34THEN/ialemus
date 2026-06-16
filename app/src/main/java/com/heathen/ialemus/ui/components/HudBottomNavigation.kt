package com.heathen.ialemus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heathen.ialemus.ui.navigation.AppDestination
import com.heathen.ialemus.ui.theme.CompactLayout
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.showDockLabels

@Composable
fun HudBottomNavigation(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val showLabels = showDockLabels()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(tokens.surfaceDeep.copy(alpha = 0.92f))
            .padding(horizontal = 4.dp, vertical = CompactLayout.dockVerticalPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppDestination.dockDestinations.forEach { destination ->
            HudNavItem(
                destination = destination,
                selected = selected == destination,
                showLabel = showLabels,
                onClick = { onSelect(destination) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HudNavItem(
    destination: AppDestination,
    selected: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val iconTint = if (selected) tokens.navActive else tokens.textMuted.copy(alpha = 0.75f)
    val labelColor = if (selected) tokens.accentActive else tokens.textMuted.copy(alpha = 0.7f)

    Column(
        modifier = modifier
            .height(CompactLayout.dockMinTouchTarget)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = iconTint,
            modifier = Modifier
                .size(CompactLayout.dockIconSize)
                .alpha(if (selected) 1f else 0.85f),
        )
        if (showLabel) {
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = CompactLayout.dockLabelFontSize,
                ),
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            modifier = Modifier
                .padding(top = if (showLabel) 3.dp else 4.dp)
                .width(CompactLayout.dockIndicatorWidth)
                .height(CompactLayout.dockIndicatorHeight)
                .background(
                    color = if (selected) tokens.accentActive else tokens.hudBorderColor.copy(alpha = 0f),
                    shape = RoundedCornerShape(50),
                ),
        )
    }
}
