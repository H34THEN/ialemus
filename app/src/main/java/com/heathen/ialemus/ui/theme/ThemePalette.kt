package com.heathen.ialemus.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.heathen.ialemus.core.model.ThemeId

// TODO: Refine typography, shapes, and HUD density per theme.

fun colorSchemeFor(themeId: ThemeId): ColorScheme = when (themeId) {
    ThemeId.ARCHIVE_BLACK -> darkColorScheme(
        primary = ArchiveBlackPrimary,
        secondary = ArchiveBlackSecondary,
        background = ArchiveBlackBackground,
        surface = ArchiveBlackSurface,
        onBackground = ArchiveBlackOnBackground,
        onSurface = ArchiveBlackOnBackground,
    )
    ThemeId.GHOST_IN_THE_CODE -> darkColorScheme(
        primary = GhostGlowPrimary,
        secondary = GhostGlowSecondary,
        background = GhostGlowBackground,
        surface = GhostGlowBackground.copy(alpha = 0.9f),
        onBackground = Color.White,
        onSurface = Color.White,
    )
    ThemeId.TERMINAL_KITTIE -> darkColorScheme(
        primary = TerminalGreen,
        secondary = TerminalGreen.copy(alpha = 0.7f),
        background = TerminalBackground,
        surface = TerminalBackground.copy(alpha = 0.95f),
        onBackground = TerminalGreen,
        onSurface = TerminalGreen,
    )
    ThemeId.CHTHONIC_SIGNAL -> darkColorScheme(
        primary = ChthonicCrimson,
        secondary = ChthonicEmber,
        background = ChthonicBackground,
        surface = ChthonicBackground.copy(alpha = 0.92f),
        onBackground = Color(0xFFF5F5DC),
        onSurface = Color(0xFFF5F5DC),
    )
    ThemeId.NEON_OSSUARY -> darkColorScheme(
        primary = NeonViolet,
        secondary = NeonCyan,
        tertiary = NeonMagenta,
        background = NeonOssuaryBackground,
        surface = NeonOssuaryBackground.copy(alpha = 0.9f),
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
    )
    ThemeId.FALLOUT_CRT -> darkColorScheme(
        primary = FalloutAmber,
        secondary = FalloutGreen,
        background = FalloutBackground,
        surface = FalloutBackground.copy(alpha = 0.95f),
        onBackground = FalloutAmber,
        onSurface = FalloutAmber,
    )
    ThemeId.CYBER_SHRINE -> darkColorScheme(
        primary = ShrineGold,
        secondary = ShrineRed,
        background = ShrineBackground,
        surface = ShrineBackground.copy(alpha = 0.93f),
        onBackground = Color(0xFFFAFAF9),
        onSurface = Color(0xFFFAFAF9),
    )
    ThemeId.CANDY_MALWARE -> darkColorScheme(
        primary = CandyPink,
        secondary = CandyBlue,
        tertiary = CandyAcid,
        background = CandyBackground,
        surface = CandyBackground.copy(alpha = 0.9f),
        onBackground = Color.White,
        onSurface = Color.White,
    )
}
