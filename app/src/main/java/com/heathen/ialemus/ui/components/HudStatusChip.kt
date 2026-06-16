package com.heathen.ialemus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudStatusChip(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    warning: Boolean = false,
    disabled: Boolean = false,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = when {
        disabled -> tokens.hudBorderColor.copy(alpha = 0.3f)
        warning -> tokens.warningColor
        highlighted -> tokens.successAccent
        else -> tokens.hudBorderColor
    }
    val textColor = when {
        disabled -> tokens.textMuted
        warning -> tokens.warningColor
        highlighted -> tokens.successAccent
        else -> tokens.textPrimary.copy(alpha = 0.85f)
    }
    Surface(
        modifier = modifier.border(1.dp, borderColor, MaterialTheme.shapes.small),
        color = tokens.panelOverlay.copy(alpha = if (disabled) 0.5f else 0.75f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) = HudStatusChip(label = label, modifier = modifier, highlighted = highlighted)
