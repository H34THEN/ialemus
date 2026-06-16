package com.heathen.ialemus.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.heathen.ialemus.core.model.ThemeId

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
    val surfaceDeep: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accentActive: Color,
    val navActive: Color,
    val showGrid: Boolean = true,
    val showScanlines: Boolean = true,
)

val LocalIalemusTokens = compositionLocalOf { eva01Tokens() }

fun tokensFor(themeId: ThemeId, dapMode: Boolean = false): IalemusThemeTokens {
    val base = when (themeId) {
        ThemeId.EVA_01_BERSERK -> eva01Tokens()
        ThemeId.EVA_00_PROTOTYPE -> IalemusThemeTokens(
            glowColor = Eva00Primary,
            warningColor = Eva00Primary,
            scanlineColor = Color(0x1AFFC107),
            terminalGridColor = Color(0x22FFC107),
            waveformColor = Eva00Secondary,
            dangerAccent = Color(0xFFFF5252),
            successAccent = Eva00Primary,
            hudBorderColor = Eva00Primary.copy(alpha = 0.5f),
            panelOverlay = Eva00Surface,
            surfaceDeep = Eva00Background,
            textPrimary = Eva00OnBackground,
            textMuted = Eva00OnBackground.copy(alpha = 0.65f),
            accentActive = Eva00Primary,
            navActive = Eva00Primary,
        )
        ThemeId.EVA_02_ASUKA_RED -> IalemusThemeTokens(
            glowColor = Eva02Primary,
            warningColor = Eva02Secondary,
            scanlineColor = Color(0x1AE53935),
            terminalGridColor = Color(0x22E53935),
            waveformColor = Eva02Secondary,
            dangerAccent = Eva02Primary,
            successAccent = Color(0xFFFFAB40),
            hudBorderColor = Eva02Primary.copy(alpha = 0.55f),
            panelOverlay = Eva02Surface,
            surfaceDeep = Eva02Background,
            textPrimary = Eva02OnBackground,
            textMuted = Eva02OnBackground.copy(alpha = 0.65f),
            accentActive = Eva02Secondary,
            navActive = Eva02Primary,
        )
        ThemeId.EVA_03_SHADOW -> IalemusThemeTokens(
            glowColor = Eva03Secondary,
            warningColor = Eva03Primary,
            scanlineColor = Color(0x1A5C6BC0),
            terminalGridColor = Color(0x227E57C2),
            waveformColor = Eva03Primary,
            dangerAccent = Color(0xFFFF5252),
            successAccent = Eva03Primary,
            hudBorderColor = Eva03Secondary.copy(alpha = 0.5f),
            panelOverlay = Eva03Surface,
            surfaceDeep = Eva03Background,
            textPrimary = Eva03OnBackground,
            textMuted = Eva03OnBackground.copy(alpha = 0.65f),
            accentActive = Eva03Secondary,
            navActive = Eva03Primary,
        )
        ThemeId.EVA_05_MASS_PRODUCTION -> IalemusThemeTokens(
            glowColor = Eva05Primary,
            warningColor = Eva05Tertiary,
            scanlineColor = Color(0x14FFFFFF),
            terminalGridColor = Color(0x22BDBDBD),
            waveformColor = Eva05Secondary,
            dangerAccent = Eva05Tertiary,
            successAccent = Eva05Primary,
            hudBorderColor = Eva05Secondary.copy(alpha = 0.45f),
            panelOverlay = Eva05Surface,
            surfaceDeep = Eva05Background,
            textPrimary = Eva05OnBackground,
            textMuted = Eva05OnBackground.copy(alpha = 0.65f),
            accentActive = Eva05Primary,
            navActive = Eva05Tertiary,
        )
        ThemeId.TERMINAL_DOGMA -> IalemusThemeTokens(
            glowColor = DogmaPrimary,
            warningColor = DogmaSecondary,
            scanlineColor = Color(0x1AFF1744),
            terminalGridColor = Color(0x22FF1744),
            waveformColor = DogmaSecondary,
            dangerAccent = DogmaPrimary,
            successAccent = DogmaSecondary,
            hudBorderColor = DogmaPrimary.copy(alpha = 0.6f),
            panelOverlay = DogmaSurface,
            surfaceDeep = DogmaBackground,
            textPrimary = DogmaOnBackground,
            textMuted = DogmaOnBackground.copy(alpha = 0.65f),
            accentActive = DogmaSecondary,
            navActive = DogmaPrimary,
        )
        ThemeId.TACTICAL_COMMAND -> IalemusThemeTokens(
            glowColor = TacticalPrimary,
            warningColor = TacticalSecondary,
            scanlineColor = Color(0x1AFF5722),
            terminalGridColor = Color(0x22FF5722),
            waveformColor = TacticalPrimary,
            dangerAccent = Color(0xFFD50000),
            successAccent = Color(0xFFFFAB00),
            hudBorderColor = TacticalPrimary.copy(alpha = 0.55f),
            panelOverlay = TacticalSurface,
            surfaceDeep = TacticalBackground,
            textPrimary = TacticalOnBackground,
            textMuted = TacticalOnBackground.copy(alpha = 0.65f),
            accentActive = TacticalSecondary,
            navActive = TacticalPrimary,
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
            surfaceDeep = ArchiveBlackBackground,
            textPrimary = ArchiveBlackOnBackground,
            textMuted = ArchiveBlackOnBackground.copy(alpha = 0.65f),
            accentActive = GhostGlowPrimary,
            navActive = GhostGlowSecondary,
        )
    }
    return if (dapMode) {
        base.copy(showGrid = false, showScanlines = false)
    } else {
        base
    }
}

private fun eva01Tokens() = IalemusThemeTokens(
    glowColor = Eva01Primary,
    warningColor = Eva01Tertiary,
    scanlineColor = Color(0x1A39FF14),
    terminalGridColor = Color(0x2239FF14),
    waveformColor = Eva01Secondary,
    dangerAccent = Eva01Error,
    successAccent = Eva01Secondary,
    hudBorderColor = Eva01Primary.copy(alpha = 0.55f),
    panelOverlay = Eva01Surface.copy(alpha = 0.94f),
    surfaceDeep = Eva01Background,
    textPrimary = Eva01OnBackground,
    textMuted = Eva01OnBackground.copy(alpha = 0.68f),
    accentActive = Eva01Secondary,
    navActive = Eva01Secondary,
)
