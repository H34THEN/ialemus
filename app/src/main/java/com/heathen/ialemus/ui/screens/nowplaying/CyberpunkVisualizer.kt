package com.heathen.ialemus.ui.screens.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.NowPlayingVisualizerMode
import com.heathen.ialemus.core.player.PlaybackState
import com.heathen.ialemus.core.visualizer.AudioVisualizerState
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberpunkVisualizerPanel(
    mode: NowPlayingVisualizerMode,
    playbackState: PlaybackState,
    visualizerState: AudioVisualizerState,
    dapMode: Boolean,
    onCycleMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val effectiveMode = if (dapMode && mode != NowPlayingVisualizerMode.STATIC_HUD) {
        NowPlayingVisualizerMode.STATIC_HUD
    } else {
        mode
    }
    val animEnabled = playbackState.isPlaying && !dapMode && effectiveMode != NowPlayingVisualizerMode.STATIC_HUD
    val intensity = remember(visualizerState.barLevels) {
        visualizerState.barLevels.average().toFloat().coerceIn(0.05f, 1f)
    }
    val phase = remember(playbackState.positionMs, intensity) {
        ((playbackState.positionMs % 4000L) / 4000f + intensity * 0.25f) % 1f
    }

    HudPanel(
        title = "Signal Visualizer",
        sectionTag = "AUDIO LINK",
        subtitle = effectiveMode.label,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, tokens.hudBorderColor.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                .padding(4.dp),
        ) {
            when (effectiveMode) {
                NowPlayingVisualizerMode.SIGNAL_BARS -> SignalBarsVisualizer(
                    levels = visualizerState.barLevels,
                    playing = animEnabled,
                    active = tokens.accentActive,
                    glow = tokens.glowColor,
                )
                NowPlayingVisualizerMode.RADAR_SWEEP -> RadarSweepVisualizer(
                    phase = phase,
                    intensity = intensity,
                    playing = animEnabled,
                    active = tokens.accentActive,
                    warning = tokens.warningColor,
                )
                NowPlayingVisualizerMode.WAVE_TRACE -> WaveTraceVisualizer(
                    waveform = visualizerState.waveform,
                    phase = phase,
                    playing = animEnabled,
                    glow = tokens.glowColor,
                    active = tokens.accentActive,
                )
                NowPlayingVisualizerMode.HEX_PULSE -> HexPulseVisualizer(
                    intensity = intensity,
                    phase = phase,
                    playing = animEnabled,
                    active = tokens.accentActive,
                    border = tokens.hudBorderColor,
                )
                NowPlayingVisualizerMode.SPECTRUM_TUNNEL -> SpectrumTunnelVisualizer(
                    levels = visualizerState.barLevels,
                    phase = phase,
                    playing = animEnabled,
                    active = tokens.accentActive,
                    glow = tokens.glowColor,
                )
                NowPlayingVisualizerMode.STATIC_HUD -> StaticHudVisualizer(
                    playing = playbackState.isPlaying,
                    active = tokens.accentActive,
                    muted = tokens.textMuted,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 6.dp)) {
            val signalLabel = when {
                visualizerState.isReactiveAudio -> "REACTIVE AUDIO"
                visualizerState.fallbackReason != null -> visualizerState.fallbackReason.uppercase()
                else -> "SIMULATED SIGNAL"
            }
            Text(
                text = if (playbackState.isPlaying) {
                    "$signalLabel · ${playbackState.playbackSpeed}x"
                } else {
                    "SIGNAL IDLE"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (playbackState.isPlaying) tokens.glowColor else tokens.textMuted,
            )
            if (onCycleMode != null) {
                HudButton(
                    label = "Cycle Visualizer",
                    onClick = onCycleMode,
                    accent = HudButtonAccent.Neutral,
                )
            }
        }
    }
}

@Composable
private fun SignalBarsVisualizer(
    levels: List<Float>,
    playing: Boolean,
    active: Color,
    glow: Color,
) {
    Canvas(Modifier.fillMaxSize()) {
        val barCount = levels.size.coerceAtLeast(1)
        val barWidth = size.width / (barCount * 1.6f)
        val alphaBase = if (playing) 1f else 0.35f
        levels.forEachIndexed { index, level ->
            val height = size.height * level.coerceIn(0.08f, 1f) * alphaBase
            val left = index * (barWidth * 1.6f) + barWidth * 0.3f
            drawRect(
                color = if (index % 3 == 0) glow else active,
                topLeft = Offset(left, size.height - height),
                size = androidx.compose.ui.geometry.Size(barWidth, height),
                alpha = alphaBase,
            )
        }
    }
}

@Composable
private fun RadarSweepVisualizer(
    phase: Float,
    intensity: Float,
    playing: Boolean,
    active: Color,
    warning: Color,
) {
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * (0.35f + intensity * 0.12f)
        drawCircle(color = active.copy(alpha = 0.2f), radius = radius, center = center, style = Stroke(1.5f))
        drawCircle(color = active.copy(alpha = 0.12f), radius = radius * 0.66f, center = center, style = Stroke(1f))
        if (playing) {
            rotate(degrees = phase * 360f, pivot = center) {
                drawLine(color = warning, start = center, end = Offset(center.x + radius, center.y), strokeWidth = 2f)
            }
        }
        repeat(6) { i ->
            val a = (i / 6f) * PI.toFloat() * 2f
            val dotRadius = 2f + intensity * 3f
            drawCircle(
                color = active,
                radius = dotRadius,
                center = Offset(center.x + cos(a) * radius * 0.7f, center.y + sin(a) * radius * 0.7f),
                alpha = if (playing) 0.9f else 0.3f,
            )
        }
    }
}

@Composable
private fun WaveTraceVisualizer(
    waveform: List<Float>,
    phase: Float,
    playing: Boolean,
    glow: Color,
    active: Color,
) {
    Canvas(Modifier.fillMaxSize()) {
        val path = Path()
        val steps = waveform.size.coerceAtLeast(16)
        path.moveTo(0f, size.height / 2f)
        repeat(steps + 1) { step ->
            val x = size.width * step / steps
            val sample = waveform.getOrNull(step % waveform.size) ?: 0f
            val fallback = sin((step * 0.35f + phase * PI * 2).toDouble()).toFloat()
            val amp = if (waveform.isNotEmpty()) sample else fallback
            val y = size.height / 2f + amp * size.height * 0.32f * (if (playing) 1f else 0.2f)
            path.lineTo(x, y)
        }
        drawPath(path, color = glow, style = Stroke(2f), alpha = if (playing) 1f else 0.4f)
        drawPath(path, color = active, style = Stroke(1f), alpha = 0.5f)
    }
}

@Composable
private fun HexPulseVisualizer(
    intensity: Float,
    phase: Float,
    playing: Boolean,
    active: Color,
    border: Color,
) {
    Canvas(Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        repeat(3) { ring ->
            val pulse = if (playing) 0.75f + 0.25f * intensity + 0.1f * sin((phase * PI * 2 + ring).toDouble()).toFloat() else 0.5f
            val radius = size.minDimension * (0.18f + ring * 0.14f) * pulse
            drawHexagon(center = Offset(cx, cy), radius = radius, color = if (ring == 1) active else border, alpha = if (playing) 0.8f else 0.35f)
        }
    }
}

@Composable
private fun SpectrumTunnelVisualizer(
    levels: List<Float>,
    phase: Float,
    playing: Boolean,
    active: Color,
    glow: Color,
) {
    Canvas(Modifier.fillMaxSize()) {
        repeat(8) { band ->
            val level = levels.getOrElse(band % levels.size) { 0.2f }
            val t = ((band + phase + level * 0.2f) % 1f)
            val inset = size.width * t * 0.45f
            val top = size.height * 0.15f + band * 4f
            val bottom = size.height * 0.85f - band * 4f
            drawRect(
                color = if (band % 2 == 0) active else glow,
                topLeft = Offset(inset, top),
                size = androidx.compose.ui.geometry.Size(size.width - inset * 2f, bottom - top),
                alpha = (if (playing) 0.55f else 0.2f) * (1f - t * 0.7f) * level.coerceIn(0.3f, 1f),
                style = Stroke(1.5f),
            )
        }
    }
}

@Composable
private fun StaticHudVisualizer(playing: Boolean, active: Color, muted: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val gridColor = if (playing) active.copy(alpha = 0.35f) else muted.copy(alpha = 0.25f)
        val step = size.width / 8f
        var x = 0f
        while (x <= size.width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
            y += size.height / 5f
        }
        drawLine(if (playing) active else muted, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), 1.5f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagon(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float,
) {
    val path = Path()
    repeat(6) { i ->
        val angle = (PI / 3.0 * i - PI / 6.0).toFloat()
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color, style = Stroke(2f), alpha = alpha)
}
