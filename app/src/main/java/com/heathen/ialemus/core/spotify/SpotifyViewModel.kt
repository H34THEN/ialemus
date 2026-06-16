package com.heathen.ialemus.core.spotify

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.ui.util.openSpotifyAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpotifyViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val authRepository = container.spotifyAuthRepository
    private val apiClient = container.spotifyApiClient
    private val settingsRepository = container.settingsRepository

    private val _playback = MutableStateFlow<SpotifyPlaybackState?>(null)
    private val _noActiveDevice = MutableStateFlow(false)
    private val _playbackError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SpotifyUiState> = combine(
        authRepository.authState,
        settingsRepository.spotifySettings,
        _playback,
        _noActiveDevice,
        _playbackError,
    ) { auth, settings, playback, noDevice, playbackError ->
        val clientId = settings.clientId.ifBlank { SpotifyDefaults.CLIENT_ID }
        val redirectUri = SpotifyDefaults.REDIRECT_URI
        val profile = if (auth.profileId.isNotBlank() || auth.displayName.isNotBlank()) {
            SpotifyProfile(
                id = auth.profileId,
                displayName = auth.displayName.ifBlank { "Spotify User" },
                email = auth.email.takeIf { it.isNotBlank() },
                product = auth.product.takeIf { it.isNotBlank() },
                country = auth.country.takeIf { it.isNotBlank() },
                imageUrl = auth.profileImageUrl.takeIf { it.isNotBlank() },
            )
        } else {
            null
        }
        val status = when {
            auth.lastError != null && !auth.hasTokens -> SpotifyConnectionStatus.ERROR
            auth.sessionExpired -> SpotifyConnectionStatus.SESSION_EXPIRED
            auth.authInProgress -> SpotifyConnectionStatus.AUTH_IN_PROGRESS
            auth.hasTokens && !auth.sessionExpired -> SpotifyConnectionStatus.CONNECTED
            else -> SpotifyConnectionStatus.READY
        }
        SpotifyUiState(
            clientId = clientId,
            redirectUri = redirectUri,
            connectionStatus = status,
            authInProgress = auth.authInProgress,
            profile = profile,
            playback = playback,
            noActiveDevice = noDevice,
            sessionExpired = auth.sessionExpired,
            errorMessage = auth.lastError ?: playbackError,
            tokenExpiresAtMs = auth.expiresAtEpochMs,
            hasRefreshToken = auth.refreshToken.isNotBlank(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpotifyUiState(),
    )

    fun effectiveClientId(): String =
        uiState.value.clientId.ifBlank { SpotifyDefaults.CLIENT_ID }

    fun startLogin(context: Context) {
        viewModelScope.launch {
            val clientId = effectiveClientId()
            val session = SpotifyPkce.createSession()
            authRepository.savePendingPkce(session)
            val authUrl = buildAuthUrl(clientId, session)
            openSpotifyAuth(context, authUrl)
        }
    }

    fun handleAuthCallback(uri: Uri) {
        viewModelScope.launch {
            val auth = authRepository.authState.first()
            val error = uri.getQueryParameter("error")
            if (!error.isNullOrBlank()) {
                val description = uri.getQueryParameter("error_description") ?: error
                authRepository.setAuthError("Spotify login failed: $description")
                return@launch
            }
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if (code.isNullOrBlank()) {
                authRepository.setAuthError("Spotify login failed: missing authorization code.")
                return@launch
            }
            if (state.isNullOrBlank() || state != auth.pendingState) {
                authRepository.setAuthError("Spotify login failed: invalid state. Try again.")
                return@launch
            }
            if (auth.pendingCodeVerifier.isBlank()) {
                authRepository.setAuthError("Spotify login failed: missing PKCE verifier.")
                return@launch
            }
            val clientId = effectiveClientId()
            val result = apiClient.exchangeAuthorizationCode(
                code = code,
                clientId = clientId,
                redirectUri = SpotifyDefaults.REDIRECT_URI,
                codeVerifier = auth.pendingCodeVerifier,
            )
            result.fold(
                onSuccess = { token ->
                    authRepository.saveTokens(token, auth.refreshToken)
                    settingsRepository.saveSpotifySettings(
                        com.heathen.ialemus.core.settings.SpotifySettings(
                            clientId = clientId,
                            displayName = auth.displayName,
                            connected = true,
                        ),
                    )
                    loadProfileAndPlayback()
                },
                onFailure = { error ->
                    authRepository.setAuthError(
                        error.message ?: "Spotify token exchange failed.",
                    )
                },
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.clearSession()
            _playback.value = null
            _noActiveDevice.value = false
            _playbackError.value = null
            settingsRepository.saveSpotifySettings(
                com.heathen.ialemus.core.settings.SpotifySettings(
                    clientId = effectiveClientId(),
                    connected = false,
                ),
            )
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            authRepository.clearSession()
            _playback.value = null
            _noActiveDevice.value = false
            _playbackError.value = null
            settingsRepository.saveSpotifySettings(
                com.heathen.ialemus.core.settings.SpotifySettings(
                    clientId = "",
                    connected = false,
                ),
            )
        }
    }

    fun saveClientIdOverride(clientId: String) {
        viewModelScope.launch {
            settingsRepository.saveSpotifySettings(
                com.heathen.ialemus.core.settings.SpotifySettings(
                    clientId = clientId.trim(),
                    displayName = uiState.value.profile?.displayName.orEmpty(),
                    connected = uiState.value.connectionStatus == SpotifyConnectionStatus.CONNECTED,
                ),
            )
        }
    }

    fun refreshPlayback() {
        viewModelScope.launch { loadProfileAndPlayback() }
    }

    fun remoteAction(action: String) {
        viewModelScope.launch {
            val token = ensureValidAccessToken() ?: return@launch
            val result = when (action) {
                "play" -> apiClient.play(token)
                "pause" -> apiClient.pause(token)
                "next" -> apiClient.next(token)
                "previous" -> apiClient.previous(token)
                "shuffle" -> apiClient.setShuffle(token, enabled = true)
                "repeat" -> Result.failure(SpotifyApiException(0, "Repeat toggle not implemented yet"))
                else -> Result.failure(IllegalArgumentException("Unknown action"))
            }
            result.fold(
                onSuccess = { loadProfileAndPlayback() },
                onFailure = { error ->
                    _playbackError.value = error.message ?: "Spotify control failed."
                },
            )
        }
    }

    private suspend fun loadProfileAndPlayback() {
        val token = ensureValidAccessToken() ?: return
        apiClient.fetchProfile(token).onSuccess { profile ->
            authRepository.saveProfile(profile)
            settingsRepository.saveSpotifySettings(
                com.heathen.ialemus.core.settings.SpotifySettings(
                    clientId = effectiveClientId(),
                    displayName = profile.displayName,
                    connected = true,
                ),
            )
        }
        when (val result = apiClient.fetchCurrentlyPlaying(token).getOrNull()) {
            is SpotifyPlaybackResult.Active -> {
                _playback.value = result.state
                _noActiveDevice.value = false
                _playbackError.value = null
            }
            SpotifyPlaybackResult.None, SpotifyPlaybackResult.NoDevice -> {
                when (val player = apiClient.fetchPlayer(token).getOrNull()) {
                    is SpotifyPlaybackResult.Active -> {
                        _playback.value = player.state
                        _noActiveDevice.value = false
                    }
                    SpotifyPlaybackResult.NoDevice, SpotifyPlaybackResult.None -> {
                        _playback.value = null
                        _noActiveDevice.value = true
                    }
                    SpotifyPlaybackResult.Unauthorized -> markSessionExpired()
                    SpotifyPlaybackResult.Forbidden -> {
                        _playbackError.value = "Spotify playback unavailable for this account/device."
                    }
                    null -> {
                        _playback.value = null
                        _noActiveDevice.value = true
                    }
                }
            }
            SpotifyPlaybackResult.Unauthorized -> markSessionExpired()
            SpotifyPlaybackResult.Forbidden -> {
                _playbackError.value = "Insufficient Spotify scopes or Premium required."
            }
            null -> {
                _playback.value = null
                _noActiveDevice.value = true
            }
        }
    }

    private suspend fun ensureValidAccessToken(): String? {
        val auth = authRepository.authState.first()
        if (auth.accessToken.isBlank()) return null
        if (auth.isTokenValid) return auth.accessToken
        if (auth.refreshToken.isBlank()) {
            markSessionExpired()
            return null
        }
        val refreshResult = apiClient.refreshAccessToken(auth.refreshToken, effectiveClientId())
        return refreshResult.fold(
            onSuccess = { token ->
                authRepository.saveTokens(token, auth.refreshToken)
                token.accessToken
            },
            onFailure = {
                markSessionExpired()
                null
            },
        )
    }

    private suspend fun markSessionExpired() {
        authRepository.setSessionExpired(true)
        settingsRepository.saveSpotifySettings(
            com.heathen.ialemus.core.settings.SpotifySettings(
                clientId = effectiveClientId(),
                connected = false,
            ),
        )
    }

    init {
        viewModelScope.launch {
            val auth = authRepository.authState.first()
            if (auth.hasTokens && !auth.sessionExpired) {
                loadProfileAndPlayback()
            }
        }
    }

    private fun buildAuthUrl(clientId: String, session: PkceSession): String = buildString {
        append(SpotifyDefaults.AUTH_BASE)
        append("?client_id=").append(Uri.encode(clientId))
        append("&response_type=code")
        append("&redirect_uri=").append(Uri.encode(SpotifyDefaults.REDIRECT_URI))
        append("&code_challenge_method=S256")
        append("&code_challenge=").append(Uri.encode(session.codeChallenge))
        append("&state=").append(Uri.encode(session.state))
        append("&scope=").append(Uri.encode(SpotifyDefaults.SCOPES))
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpotifyViewModel::class.java)) {
                return SpotifyViewModel(container) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
