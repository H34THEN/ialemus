package com.heathen.ialemus.core.spotify

object SpotifyDefaults {
    /** Personal-app Client ID — override in Settings if needed. Not a secret. */
    const val CLIENT_ID = "9c6067114c44430fba5b6a627a907e61"
    const val REDIRECT_URI = "ialemus://spotify-auth-callback"
    const val REDIRECT_SCHEME = "ialemus"
    const val REDIRECT_HOST = "spotify-auth-callback"
    const val PACKAGE_NAME = "com.heathen.ialemus"
    const val SPOTIFY_PACKAGE = "com.spotify.music"
    const val AUTH_BASE = "https://accounts.spotify.com/authorize"
    const val TOKEN_URL = "https://accounts.spotify.com/api/token"
    const val API_BASE = "https://api.spotify.com/v1"

    const val SCOPES = "user-read-private user-read-email " +
        "user-read-playback-state user-modify-playback-state user-read-currently-playing " +
        "playlist-read-private playlist-read-collaborative user-library-read"

    /** Minutes before expiry to proactively refresh. */
    const val REFRESH_BUFFER_MS = 60_000L
}

enum class SpotifyConnectionStatus(val label: String) {
    READY("Ready to connect"),
    AUTH_IN_PROGRESS("Waiting for authorization"),
    CONNECTED("Connected"),
    SESSION_EXPIRED("Session expired"),
    ERROR("Error"),
}
