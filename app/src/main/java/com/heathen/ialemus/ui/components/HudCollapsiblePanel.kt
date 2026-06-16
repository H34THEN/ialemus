package com.heathen.ialemus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudCollapsiblePanel(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    sectionTag: String? = null,
    subtitle: String? = null,
    statusLabel: String? = null,
    defaultExpandedGuide: Boolean = false,
    content: @Composable () -> Unit,
) {
    val tokens = LocalIalemusTokens.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, tokens.hudBorderColor, MaterialTheme.shapes.medium),
        color = tokens.panelOverlay,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (sectionTag != null) {
                        Text(
                            text = sectionTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.accentActive,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.glowColor,
                        fontWeight = FontWeight.Bold,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (statusLabel != null) {
                        HudStatusChip(
                            label = statusLabel,
                            highlighted = expanded,
                            warning = !expanded && defaultExpandedGuide,
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = tokens.accentActive,
                    )
                    Text(
                        text = if (expanded) "CLOSE" else "OPEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.accentActive,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}
