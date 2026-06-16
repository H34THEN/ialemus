package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = if (highlighted) tokens.successAccent else tokens.hudBorderColor
    val textColor = if (highlighted) tokens.successAccent else MaterialTheme.colorScheme.onSurface.copy(0.8f)
    Surface(
        modifier = modifier.border(1.dp, borderColor, MaterialTheme.shapes.small),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
