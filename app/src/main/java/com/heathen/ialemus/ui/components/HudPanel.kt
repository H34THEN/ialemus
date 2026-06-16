package com.heathen.ialemus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun HudPanel(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    sectionTag: String? = null,
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
            if (sectionTag != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = sectionTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.accentActive,
                        fontWeight = FontWeight.Bold,
                    )
                    if (tokens.useHexAccents) {
                        HexClusterAccent(count = 2, size = 7.dp)
                    }
                }
            }
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = tokens.glowColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }
            content()
        }
    }
}
