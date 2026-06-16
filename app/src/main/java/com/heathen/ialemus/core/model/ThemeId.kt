package com.heathen.ialemus.core.model

enum class ThemeId(val displayName: String, val group: ThemeGroup) {
    // EVA-inspired (default group)
    EVA_01_BERSERK("EVA-01 Berserk", ThemeGroup.EVA),
    EVA_00_PROTOTYPE("EVA-00 Prototype", ThemeGroup.EVA),
    EVA_02_ASUKA_RED("EVA-02 Asuka Red", ThemeGroup.EVA),
    EVA_03_SHADOW("EVA-03 Shadow", ThemeGroup.EVA),
    EVA_05_MASS_PRODUCTION("EVA-05 Mass Production", ThemeGroup.EVA),
    TERMINAL_DOGMA("Terminal Dogma", ThemeGroup.EVA),
    TACTICAL_COMMAND("Tactical Command", ThemeGroup.EVA),

    // Original Ialemus themes
    GHOST_IN_THE_CODE("Ghost in the Code", ThemeGroup.IALEMUS),
    TERMINAL_KITTIE("Terminal Kittie", ThemeGroup.IALEMUS),
    CHTHONIC_SIGNAL("Chthonic Signal", ThemeGroup.IALEMUS),
    NEON_OSSUARY("Neon Ossuary", ThemeGroup.IALEMUS),
    ARCHIVE_BLACK("Archive Black", ThemeGroup.IALEMUS),
    FALLOUT_CRT("Fallout CRT", ThemeGroup.IALEMUS),
    CYBER_SHRINE("Cyber Shrine", ThemeGroup.IALEMUS),
    CANDY_MALWARE("Candy Malware", ThemeGroup.IALEMUS),
    ;

    companion object {
        val DEFAULT = EVA_01_BERSERK
        val evaThemes = entries.filter { it.group == ThemeGroup.EVA }
        val ialemusThemes = entries.filter { it.group == ThemeGroup.IALEMUS }
    }
}

enum class ThemeGroup {
    EVA,
    IALEMUS,
}
