package com.heathen.ialemus.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.ThemeId

private val HudShapes = Shapes(
    small = CutCornerShape(4.dp),
    medium = CutCornerShape(8.dp),
    large = CutCornerShape(12.dp),
)

fun shapesFor(themeId: ThemeId): Shapes = when (themeId.group) {
    com.heathen.ialemus.core.model.ThemeGroup.EVA -> HudShapes
    else -> Shapes()
}

fun colorSchemeFor(themeId: ThemeId): ColorScheme = when (themeId) {
    ThemeId.EVA_01_BERSERK -> hudDarkColorScheme(
        primary = Eva01Primary,
        secondary = Eva01Secondary,
        tertiary = Eva01Tertiary,
        background = Eva01Background,
        surface = Eva01Surface,
        onContent = Eva01OnBackground,
        error = Eva01Error,
    )
    ThemeId.EVA_00_PROTOTYPE -> hudDarkColorScheme(
        primary = Eva00Primary,
        secondary = Eva00Secondary,
        background = Eva00Background,
        surface = Eva00Surface,
        onContent = Eva00OnBackground,
    )
    ThemeId.EVA_02_ASUKA_RED -> hudDarkColorScheme(
        primary = Eva02Primary,
        secondary = Eva02Secondary,
        background = Eva02Background,
        surface = Eva02Surface,
        onContent = Eva02OnBackground,
    )
    ThemeId.EVA_03_SHADOW -> hudDarkColorScheme(
        primary = Eva03Primary,
        secondary = Eva03Secondary,
        background = Eva03Background,
        surface = Eva03Surface,
        onContent = Eva03OnBackground,
    )
    ThemeId.EVA_05_MASS_PRODUCTION -> hudDarkColorScheme(
        primary = Eva05Primary,
        secondary = Eva05Secondary,
        tertiary = Eva05Tertiary,
        background = Eva05Background,
        surface = Eva05Surface,
        onContent = Eva05OnBackground,
        error = Eva05Tertiary,
    )
    ThemeId.TERMINAL_DOGMA -> hudDarkColorScheme(
        primary = DogmaPrimary,
        secondary = DogmaSecondary,
        background = DogmaBackground,
        surface = DogmaSurface,
        onContent = DogmaOnBackground,
        error = DogmaPrimary,
    )
    ThemeId.TACTICAL_COMMAND -> hudDarkColorScheme(
        primary = TacticalPrimary,
        secondary = TacticalSecondary,
        background = TacticalBackground,
        surface = TacticalSurface,
        onContent = TacticalOnBackground,
        error = Color(0xFFD50000),
    )
    ThemeId.ARCHIVE_BLACK -> hudDarkColorScheme(
        primary = ArchiveBlackPrimary,
        secondary = ArchiveBlackSecondary,
        background = ArchiveBlackBackground,
        surface = ArchiveBlackSurface,
        onContent = ArchiveBlackOnBackground,
    )
    ThemeId.GHOST_IN_THE_CODE -> hudDarkColorScheme(
        primary = GhostGlowPrimary,
        secondary = GhostGlowSecondary,
        background = GhostGlowBackground,
        surface = GhostGlowBackground.copy(alpha = 0.9f),
        onContent = Color.White,
    )
    ThemeId.TERMINAL_KITTIE -> hudDarkColorScheme(
        primary = TerminalGreen,
        secondary = TerminalGreen.copy(alpha = 0.7f),
        background = TerminalBackground,
        surface = TerminalBackground.copy(alpha = 0.95f),
        onContent = TerminalGreen,
    )
    ThemeId.CHTHONIC_SIGNAL -> hudDarkColorScheme(
        primary = ChthonicCrimson,
        secondary = ChthonicEmber,
        background = ChthonicBackground,
        surface = ChthonicBackground.copy(alpha = 0.92f),
        onContent = Color(0xFFF5F5DC),
    )
    ThemeId.NEON_OSSUARY -> hudDarkColorScheme(
        primary = NeonViolet,
        secondary = NeonCyan,
        tertiary = NeonMagenta,
        background = NeonOssuaryBackground,
        surface = NeonOssuaryBackground.copy(alpha = 0.9f),
        onContent = Color(0xFFF8FAFC),
    )
    ThemeId.FALLOUT_CRT -> hudDarkColorScheme(
        primary = FalloutAmber,
        secondary = FalloutGreen,
        background = FalloutBackground,
        surface = FalloutBackground.copy(alpha = 0.95f),
        onContent = FalloutAmber,
    )
    ThemeId.CYBER_SHRINE -> hudDarkColorScheme(
        primary = ShrineGold,
        secondary = ShrineRed,
        background = ShrineBackground,
        surface = ShrineBackground.copy(alpha = 0.93f),
        onContent = Color(0xFFFAFAF9),
    )
    ThemeId.CANDY_MALWARE -> hudDarkColorScheme(
        primary = CandyPink,
        secondary = CandyBlue,
        tertiary = CandyAcid,
        background = CandyBackground,
        surface = CandyBackground.copy(alpha = 0.9f),
        onContent = Color.White,
    )
}
