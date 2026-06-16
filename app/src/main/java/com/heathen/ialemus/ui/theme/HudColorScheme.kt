package com.heathen.ialemus.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Builds a HUD-safe dark [ColorScheme] with explicit on-* colors so Material3 never
 * picks black foreground text on violet/neon surfaces (common EVA-01 issue).
 */
fun hudDarkColorScheme(
    primary: Color,
    secondary: Color,
    background: Color,
    surface: Color,
    onContent: Color,
    tertiary: Color = secondary,
    error: Color = Color(0xFFFF5252),
): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onContent,
    primaryContainer = surface,
    onPrimaryContainer = onContent,
    secondary = secondary,
    onSecondary = background,
    secondaryContainer = surface,
    onSecondaryContainer = onContent,
    tertiary = tertiary,
    onTertiary = background,
    tertiaryContainer = surface,
    onTertiaryContainer = onContent,
    background = background,
    onBackground = onContent,
    surface = surface,
    onSurface = onContent,
    surfaceVariant = surface.copy(alpha = 0.92f),
    onSurfaceVariant = onContent.copy(alpha = 0.78f),
    error = error,
    onError = onContent,
    outline = primary.copy(alpha = 0.48f),
    outlineVariant = primary.copy(alpha = 0.28f),
    inverseSurface = onContent,
    inverseOnSurface = background,
    scrim = Color.Black.copy(alpha = 0.65f),
)
