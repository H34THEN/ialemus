package com.heathen.ialemus.core.spotify

data class SpotifyTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresInSeconds: Int,
    val tokenType: String,
    val scope: String,
)

data class SpotifyProfile(
    val id: String,
    val displayName: String,
    val email: String?,
    val product: String?,
    val country: String?,
    val imageUrl: String?,
)

data class SpotifyPlaybackState(
    val isPlaying: Boolean,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val albumArtUrl: String?,
    val deviceName: String?,
    val progressMs: Long?,
    val durationMs: Long?,
) {
    companion object {
        val NONE = SpotifyPlaybackState(
            isPlaying = false,
            trackName = "",
            artistName = "",
            albumName = "",
            albumArtUrl = null,
            deviceName = null,
            progressMs = null,
            durationMs = null,
        )
    }
}

data class SpotifyAuthState(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresAtEpochMs: Long = 0L,
    val scope: String = "",
    val pendingCodeVerifier: String = "",
    val pendingState: String = "",
    val profileId: String = "",
    val displayName: String = "",
    val email: String = "",
    val product: String = "",
    val country: String = "",
    val profileImageUrl: String = "",
    val authInProgress: Boolean = false,
    val sessionExpired: Boolean = false,
    val lastError: String? = null,
) {
    val hasTokens: Boolean get() = accessToken.isNotBlank()
    val isTokenValid: Boolean get() =
        hasTokens && System.currentTimeMillis() < expiresAtEpochMs - SpotifyDefaults.REFRESH_BUFFER_MS
}

data class SpotifyUiState(
    val clientId: String = SpotifyDefaults.CLIENT_ID,
    val redirectUri: String = SpotifyDefaults.REDIRECT_URI,
    val connectionStatus: SpotifyConnectionStatus = SpotifyConnectionStatus.READY,
    val authInProgress: Boolean = false,
    val profile: SpotifyProfile? = null,
    val playback: SpotifyPlaybackState? = null,
    val noActiveDevice: Boolean = false,
    val sessionExpired: Boolean = false,
    val errorMessage: String? = null,
    val tokenExpiresAtMs: Long = 0L,
    val hasRefreshToken: Boolean = false,
)
