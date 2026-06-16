package com.heathen.ialemus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlighted: Boolean = false,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = if (highlighted) tokens.accentActive else tokens.hudBorderColor
    Box(
        modifier = modifier
            .size(48.dp)
            .border(1.dp, borderColor, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (highlighted) tokens.accentActive else tokens.glowColor,
            )
        }
    }
}
