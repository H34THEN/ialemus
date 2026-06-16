package com.heathen.ialemus.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudDivider(modifier: Modifier = Modifier) {
    val tokens = LocalIalemusTokens.current
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
        color = tokens.hudBorderColor.copy(alpha = 0.35f),
    )
}
