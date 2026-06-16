package com.heathen.ialemus.core.settings

import com.heathen.ialemus.core.spotify.SpotifyDefaults

data class SpotifySettings(
    val clientId: String = "",
    val displayName: String = "",
    val connected: Boolean = false,
) {
    val effectiveClientId: String get() = clientId.ifBlank { SpotifyDefaults.CLIENT_ID }
    val configured: Boolean get() = effectiveClientId.isNotBlank()
}
