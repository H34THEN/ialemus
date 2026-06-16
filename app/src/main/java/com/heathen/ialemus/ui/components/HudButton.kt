package com.heathen.ialemus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: HudButtonAccent = HudButtonAccent.Primary,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = when (accent) {
        HudButtonAccent.Primary -> tokens.accentActive
        HudButtonAccent.Warning -> tokens.warningColor
        HudButtonAccent.Danger -> tokens.dangerAccent
        HudButtonAccent.Neutral -> tokens.hudBorderColor
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .border(1.5.dp, borderColor, MaterialTheme.shapes.small)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) borderColor else tokens.textMuted,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

enum class HudButtonAccent {
    Primary,
    Warning,
    Danger,
    Neutral,
}
