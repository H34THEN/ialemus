package com.heathen.ialemus.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class IalemusThemeTokens(
    val glowColor: Color,
    val warningColor: Color,
    val scanlineColor: Color,
    val terminalGridColor: Color,
    val waveformColor: Color,
    val dangerAccent: Color,
    val successAccent: Color,
    val hudBorderColor: Color,
    val panelOverlay: Color,
)

val LocalIalemusTokens = compositionLocalOf {
    IalemusThemeTokens(
        glowColor = Eva01Primary,
        warningColor = Eva01Tertiary,
        scanlineColor = Color(0x1A39FF14),
        terminalGridColor = Color(0x2239FF14),
        waveformColor = Eva01Secondary,
        dangerAccent = Eva01Error,
        successAccent = Eva01Secondary,
        hudBorderColor = Eva01Primary.copy(alpha = 0.55f),
        panelOverlay = Eva01Surface.copy(alpha = 0.92f),
    )
}

fun tokensFor(themeId: com.heathen.ialemus.core.model.ThemeId): IalemusThemeTokens = when (themeId) {
    com.heathen.ialemus.core.model.ThemeId.EVA_01_BERSERK -> IalemusThemeTokens(
        glowColor = Eva01Primary,
        warningColor = Eva01Tertiary,
        scanlineColor = Color(0x1A39FF14),
        terminalGridColor = Color(0x2239FF14),
        waveformColor = Eva01Secondary,
        dangerAccent = Eva01Error,
        successAccent = Eva01Secondary,
        hudBorderColor = Eva01Primary.copy(alpha = 0.55f),
        panelOverlay = Eva01Surface.copy(alpha = 0.92f),
    )
    com.heathen.ialemus.core.model.ThemeId.EVA_00_PROTOTYPE -> IalemusThemeTokens(
        glowColor = Eva00Primary,
        warningColor = Eva00Primary,
        scanlineColor = Color(0x1AFFC107),
        terminalGridColor = Color(0x22FFC107),
        waveformColor = Eva00Secondary,
        dangerAccent = Color(0xFFFF5252),
        successAccent = Eva00Primary,
        hudBorderColor = Eva00Primary.copy(alpha = 0.5f),
        panelOverlay = Eva00Surface,
    )
    com.heathen.ialemus.core.model.ThemeId.EVA_02_ASUKA_RED -> IalemusThemeTokens(
        glowColor = Eva02Primary,
        warningColor = Eva02Secondary,
        scanlineColor = Color(0x1AE53935),
        terminalGridColor = Color(0x22E53935),
        waveformColor = Eva02Secondary,
        dangerAccent = Eva02Primary,
        successAccent = Color(0xFFFFAB40),
        hudBorderColor = Eva02Primary.copy(alpha = 0.55f),
        panelOverlay = Eva02Surface,
    )
    com.heathen.ialemus.core.model.ThemeId.EVA_03_SHADOW -> IalemusThemeTokens(
        glowColor = Eva03Secondary,
        warningColor = Eva03Primary,
        scanlineColor = Color(0x1A5C6BC0),
        terminalGridColor = Color(0x227E57C2),
        waveformColor = Eva03Primary,
        dangerAccent = Color(0xFFFF5252),
        successAccent = Eva03Primary,
        hudBorderColor = Eva03Secondary.copy(alpha = 0.5f),
        panelOverlay = Eva03Surface,
    )
    com.heathen.ialemus.core.model.ThemeId.EVA_05_MASS_PRODUCTION -> IalemusThemeTokens(
        glowColor = Eva05Primary,
        warningColor = Eva05Tertiary,
        scanlineColor = Color(0x14FFFFFF),
        terminalGridColor = Color(0x22BDBDBD),
        waveformColor = Eva05Secondary,
        dangerAccent = Eva05Tertiary,
        successAccent = Eva05Primary,
        hudBorderColor = Eva05Secondary.copy(alpha = 0.45f),
        panelOverlay = Eva05Surface,
    )
    com.heathen.ialemus.core.model.ThemeId.TERMINAL_DOGMA -> IalemusThemeTokens(
        glowColor = DogmaPrimary,
        warningColor = DogmaSecondary,
        scanlineColor = Color(0x1AFF1744),
        terminalGridColor = Color(0x22FF1744),
        waveformColor = DogmaSecondary,
        dangerAccent = DogmaPrimary,
        successAccent = DogmaSecondary,
        hudBorderColor = DogmaPrimary.copy(alpha = 0.6f),
        panelOverlay = DogmaSurface,
    )
    com.heathen.ialemus.core.model.ThemeId.TACTICAL_COMMAND -> IalemusThemeTokens(
        glowColor = TacticalPrimary,
        warningColor = TacticalSecondary,
        scanlineColor = Color(0x1AFF5722),
        terminalGridColor = Color(0x22FF5722),
        waveformColor = TacticalPrimary,
        dangerAccent = Color(0xFFD50000),
        successAccent = Color(0xFFFFAB00),
        hudBorderColor = TacticalPrimary.copy(alpha = 0.55f),
        panelOverlay = TacticalSurface,
    )
    else -> IalemusThemeTokens(
        glowColor = GhostGlowPrimary,
        warningColor = ChthonicEmber,
        scanlineColor = Color(0x14FFFFFF),
        terminalGridColor = Color(0x18FFFFFF),
        waveformColor = GhostGlowSecondary,
        dangerAccent = ChthonicCrimson,
        successAccent = TerminalGreen,
        hudBorderColor = GhostGlowPrimary.copy(alpha = 0.4f),
        panelOverlay = ArchiveBlackSurface,
    )
}
