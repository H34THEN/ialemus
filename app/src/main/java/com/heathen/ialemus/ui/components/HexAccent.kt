package com.heathen.ialemus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HexClusterAccent(
    modifier: Modifier = Modifier,
    color: Color = LocalIalemusTokens.current.accentActive,
    count: Int = 3,
    size: Dp = 10.dp,
    strokeWidth: Dp = 1.dp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(count.coerceIn(1, 5)) { index ->
            HexAccent(
                color = color.copy(alpha = 1f - index * 0.18f),
                size = size,
                strokeWidth = strokeWidth,
            )
        }
    }
}

@Composable
fun HexAccent(
    modifier: Modifier = Modifier,
    color: Color = LocalIalemusTokens.current.accentActive,
    size: Dp = 12.dp,
    strokeWidth: Dp = 1.dp,
    filled: Boolean = false,
) {
    Canvas(modifier = modifier.size(size)) {
        val path = hexPath(size.toPx() / 2f, center)
        if (filled) {
            drawPath(path, color.copy(alpha = 0.35f))
        }
        drawPath(path, color.copy(alpha = 0.85f), style = Stroke(width = strokeWidth.toPx()))
    }
}

@Composable
fun HudHexDivider(
    modifier: Modifier = Modifier,
    color: Color = LocalIalemusTokens.current.hudBorderColor,
) {
    val tokens = LocalIalemusTokens.current
    if (!tokens.useHexAccents) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        HexAccent(color = tokens.accentActive.copy(alpha = 0.5f), size = 8.dp)
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
        ) {
            drawLine(
                color = color.copy(alpha = 0.45f),
                start = Offset(8f, 0f),
                end = Offset(size.width - 8f, 0f),
                strokeWidth = 1f,
            )
        }
        HexAccent(color = tokens.accentActive.copy(alpha = 0.5f), size = 8.dp)
    }
}

private fun hexPath(radius: Float, center: Offset): Path {
    val path = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((60.0 * i - 30.0))
        val x = center.x + radius * kotlin.math.cos(angle).toFloat()
        val y = center.y + radius * kotlin.math.sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
