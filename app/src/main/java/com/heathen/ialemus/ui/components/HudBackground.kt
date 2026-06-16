package com.heathen.ialemus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

@Composable
fun HudBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = LocalIalemusTokens.current
    val backgroundColor = MaterialTheme.colorScheme.background
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = backgroundColor)
            if (tokens.showGrid) {
                val step = 28f
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = tokens.terminalGridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                    )
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = tokens.terminalGridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                    )
                    y += step
                }
            }
            if (tokens.showScanlines) {
                var scanY = 0f
                while (scanY < size.height) {
                    drawLine(
                        color = tokens.scanlineColor,
                        start = Offset(0f, scanY),
                        end = Offset(size.width, scanY),
                        strokeWidth = 1f,
                    )
                    scanY += 4f
                }
            }
            drawRect(
                color = tokens.hudBorderColor.copy(alpha = 0.15f),
                style = Stroke(width = 2f),
            )
        }
        content()
    }
}
