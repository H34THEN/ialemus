package com.heathen.ialemus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    statusLabel: String? = null,
) {
    val tokens = LocalIalemusTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, tokens.hudBorderColor.copy(alpha = 0.4f), MaterialTheme.shapes.small)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (statusLabel != null) {
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.accentActive,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = tokens.glowColor,
            fontWeight = FontWeight.Bold,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
