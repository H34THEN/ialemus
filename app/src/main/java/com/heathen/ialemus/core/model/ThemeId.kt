package com.heathen.ialemus.core.model

enum class ThemeId(val displayName: String) {
    GHOST_IN_THE_CODE("Ghost in the Code"),
    TERMINAL_KITTIE("Terminal Kittie"),
    CHTHONIC_SIGNAL("Chthonic Signal"),
    NEON_OSSUARY("Neon Ossuary"),
    ARCHIVE_BLACK("Archive Black"),
    FALLOUT_CRT("Fallout CRT"),
    CYBER_SHRINE("Cyber Shrine"),
    CANDY_MALWARE("Candy Malware"),
    ;

    companion object {
        val DEFAULT = ARCHIVE_BLACK
    }
}
