package com.heathen.ialemus.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
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
    ThemeId.EVA_01_BERSERK -> darkColorScheme(
        primary = Eva01Primary,
        secondary = Eva01Secondary,
        tertiary = Eva01Tertiary,
        background = Eva01Background,
        surface = Eva01Surface,
        onBackground = Eva01OnBackground,
        onSurface = Eva01OnBackground,
        error = Eva01Error,
    )
    ThemeId.EVA_00_PROTOTYPE -> darkColorScheme(
        primary = Eva00Primary,
        secondary = Eva00Secondary,
        background = Eva00Background,
        surface = Eva00Surface,
        onBackground = Eva00OnBackground,
        onSurface = Eva00OnBackground,
    )
    ThemeId.EVA_02_ASUKA_RED -> darkColorScheme(
        primary = Eva02Primary,
        secondary = Eva02Secondary,
        background = Eva02Background,
        surface = Eva02Surface,
        onBackground = Eva02OnBackground,
        onSurface = Eva02OnBackground,
    )
    ThemeId.EVA_03_SHADOW -> darkColorScheme(
        primary = Eva03Primary,
        secondary = Eva03Secondary,
        background = Eva03Background,
        surface = Eva03Surface,
        onBackground = Eva03OnBackground,
        onSurface = Eva03OnBackground,
    )
    ThemeId.EVA_05_MASS_PRODUCTION -> darkColorScheme(
        primary = Eva05Primary,
        secondary = Eva05Secondary,
        tertiary = Eva05Tertiary,
        background = Eva05Background,
        surface = Eva05Surface,
        onBackground = Eva05OnBackground,
        onSurface = Eva05OnBackground,
        error = Eva05Tertiary,
    )
    ThemeId.TERMINAL_DOGMA -> darkColorScheme(
        primary = DogmaPrimary,
        secondary = DogmaSecondary,
        background = DogmaBackground,
        surface = DogmaSurface,
        onBackground = DogmaOnBackground,
        onSurface = DogmaOnBackground,
        error = DogmaPrimary,
    )
    ThemeId.TACTICAL_COMMAND -> darkColorScheme(
        primary = TacticalPrimary,
        secondary = TacticalSecondary,
        background = TacticalBackground,
        surface = TacticalSurface,
        onBackground = TacticalOnBackground,
        onSurface = TacticalOnBackground,
        error = Color(0xFFD50000),
    )
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
