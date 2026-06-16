package com.heathen.ialemus.core.settings

/**
 * Suggested LAN defaults for the user's Ugreen NAS Docker stack.
 * Used as UI placeholders and "Reset to Local Defaults" — not secrets.
 */
object LocalServiceDefaults {
    const val METUBE = "http://192.168.1.213:38245/"
    const val SLSKD = "http://192.168.1.213:5031/"
    const val NAS_UI = "http://baphomet.local:9999/"
    const val BRIDGE_FUTURE = "http://192.168.1.213:8787"

    fun asSettings(): NasConnectionSettings = NasConnectionSettings(
        nasDisplayName = "Ugreen NAS",
        bridgeUrl = BRIDGE_FUTURE,
        bridgeToken = "",
        meTubeUrl = METUBE,
        slskdUrl = SLSKD,
        nasUiUrl = NAS_UI,
        connectionMode = com.heathen.ialemus.core.model.ConnectionMode.LOCAL_LAN,
    )
}
