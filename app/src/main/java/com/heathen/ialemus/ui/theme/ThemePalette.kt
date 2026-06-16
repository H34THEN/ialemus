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

/** Primary / secondary / tertiary swatches for theme selector preview dots. */
fun previewColorsFor(themeId: ThemeId): List<Color> = when (themeId) {
    ThemeId.EVA_01_BERSERK -> listOf(Eva01Primary, Eva01Secondary, Eva01Tertiary)
    ThemeId.EVA_00_PROTOTYPE -> listOf(Eva00Primary, Eva00Secondary, Eva00OnBackground)
    ThemeId.EVA_02_ASUKA_RED -> listOf(Eva02Primary, Eva02Secondary, Eva02OnBackground)
    ThemeId.EVA_03_SHADOW -> listOf(Eva03Primary, Eva03Secondary, Eva03OnBackground)
    ThemeId.EVA_05_MASS_PRODUCTION -> listOf(Eva05Primary, Eva05Secondary, Eva05Tertiary)
    ThemeId.TERMINAL_DOGMA -> listOf(DogmaPrimary, DogmaSecondary, DogmaOnBackground)
    ThemeId.TACTICAL_COMMAND -> listOf(TacticalPrimary, TacticalSecondary, TacticalOnBackground)
    ThemeId.GHOST_IN_THE_CODE -> listOf(GhostPrimary, GhostSecondary, GhostTertiary)
    ThemeId.TERMINAL_KITTIE -> listOf(KittiePrimary, KittieSecondary, KittieTertiary)
    ThemeId.CHTHONIC_SIGNAL -> listOf(ChthonicPrimary, ChthonicSecondary, ChthonicTertiary)
    ThemeId.NEON_OSSUARY -> listOf(OssuaryPrimary, OssuarySecondary, OssuaryTertiary)
    ThemeId.ARCHIVE_BLACK -> listOf(ArchiveBlackPrimary, ArchiveBlackSecondary, ArchiveBlackTertiary)
    ThemeId.FALLOUT_CRT -> listOf(FalloutPrimary, FalloutSecondary, FalloutTertiary)
    ThemeId.CYBER_SHRINE -> listOf(ShrinePrimary, ShrineSecondary, ShrineTertiary)
    ThemeId.CANDY_MALWARE -> listOf(CandyPrimary, CandySecondary, CandyTertiary)
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
    ThemeId.GHOST_IN_THE_CODE -> hudDarkColorScheme(
        primary = GhostPrimary,
        secondary = GhostSecondary,
        tertiary = GhostTertiary,
        background = GhostBackground,
        surface = GhostSurface,
        onContent = GhostOnBackground,
        error = GhostError,
    )
    ThemeId.TERMINAL_KITTIE -> hudDarkColorScheme(
        primary = KittiePrimary,
        secondary = KittieSecondary,
        tertiary = KittieTertiary,
        background = KittieBackground,
        surface = KittieSurface,
        onContent = KittieOnBackground,
        error = KittieError,
    )
    ThemeId.CHTHONIC_SIGNAL -> hudDarkColorScheme(
        primary = ChthonicPrimary,
        secondary = ChthonicSecondary,
        tertiary = ChthonicTertiary,
        background = ChthonicBackground,
        surface = ChthonicSurface,
        onContent = ChthonicOnBackground,
        error = ChthonicError,
    )
    ThemeId.NEON_OSSUARY -> hudDarkColorScheme(
        primary = OssuaryPrimary,
        secondary = OssuarySecondary,
        tertiary = OssuaryTertiary,
        background = OssuaryBackground,
        surface = OssuarySurface,
        onContent = OssuaryOnBackground,
        error = OssuaryError,
    )
    ThemeId.ARCHIVE_BLACK -> hudDarkColorScheme(
        primary = ArchiveBlackPrimary,
        secondary = ArchiveBlackSecondary,
        tertiary = ArchiveBlackTertiary,
        background = ArchiveBlackBackground,
        surface = ArchiveBlackSurface,
        onContent = ArchiveBlackOnBackground,
        error = ArchiveBlackError,
    )
    ThemeId.FALLOUT_CRT -> hudDarkColorScheme(
        primary = FalloutPrimary,
        secondary = FalloutSecondary,
        tertiary = FalloutTertiary,
        background = FalloutBackground,
        surface = FalloutSurface,
        onContent = FalloutOnBackground,
        error = FalloutError,
    )
    ThemeId.CYBER_SHRINE -> hudDarkColorScheme(
        primary = ShrinePrimary,
        secondary = ShrineSecondary,
        tertiary = ShrineTertiary,
        background = ShrineBackground,
        surface = ShrineSurface,
        onContent = ShrineOnBackground,
        error = ShrineError,
    )
    ThemeId.CANDY_MALWARE -> hudDarkColorScheme(
        primary = CandyPrimary,
        secondary = CandySecondary,
        tertiary = CandyTertiary,
        background = CandyBackground,
        surface = CandySurface,
        onContent = CandyOnBackground,
        error = CandyError,
    )
}
