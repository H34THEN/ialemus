package com.heathen.ialemus.core.settings

data class SpotifySettings(
    val clientId: String = "",
    val redirectUri: String = SpotifyDefaults.REDIRECT_URI,
    val displayName: String = "",
    val connected: Boolean = false,
) {
    val configured: Boolean get() = clientId.isNotBlank() && redirectUri.isNotBlank()
}

object SpotifyDefaults {
    const val REDIRECT_URI = "com.heathen.ialemus://spotify-callback"
    const val PACKAGE_NAME = "com.heathen.ialemus"
    const val AUTH_BASE = "https://accounts.spotify.com/authorize"
}

enum class SpotifyConnectionStatus(val label: String) {
    NOT_CONFIGURED("Not configured"),
    READY_TO_LOGIN("Ready to login"),
    LOGIN_REQUIRED("Login required"),
    CONNECTED("Connected"),
    SPOTIFY_APP_NOT_INSTALLED("Spotify app not installed"),
    ERROR("Error"),
}
