package com.heathen.ialemus.core.spotify

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.spotifyAuthStore: DataStore<Preferences> by preferencesDataStore(
    name = "ialemus_spotify_auth",
)

class SpotifyAuthRepository(context: Context) {
    private val dataStore = context.applicationContext.spotifyAuthStore

    val authState: Flow<SpotifyAuthState> = dataStore.data.map { prefs ->
        SpotifyAuthState(
            accessToken = prefs[KEY_ACCESS_TOKEN].orEmpty(),
            refreshToken = prefs[KEY_REFRESH_TOKEN].orEmpty(),
            expiresAtEpochMs = prefs[KEY_EXPIRES_AT] ?: 0L,
            scope = prefs[KEY_SCOPE].orEmpty(),
            pendingCodeVerifier = prefs[KEY_PENDING_VERIFIER].orEmpty(),
            pendingState = prefs[KEY_PENDING_STATE].orEmpty(),
            profileId = prefs[KEY_PROFILE_ID].orEmpty(),
            displayName = prefs[KEY_DISPLAY_NAME].orEmpty(),
            email = prefs[KEY_EMAIL].orEmpty(),
            product = prefs[KEY_PRODUCT].orEmpty(),
            country = prefs[KEY_COUNTRY].orEmpty(),
            profileImageUrl = prefs[KEY_PROFILE_IMAGE].orEmpty(),
            authInProgress = prefs[KEY_PENDING_STATE].orEmpty().isNotBlank(),
            sessionExpired = prefs[KEY_SESSION_EXPIRED] ?: false,
            lastError = prefs[KEY_LAST_ERROR],
        )
    }

    suspend fun savePendingPkce(session: PkceSession) {
        dataStore.edit { prefs ->
            prefs[KEY_PENDING_VERIFIER] = session.codeVerifier
            prefs[KEY_PENDING_STATE] = session.state
            prefs.remove(KEY_LAST_ERROR)
            prefs[KEY_SESSION_EXPIRED] = false
        }
    }

    suspend fun clearPendingPkce() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_VERIFIER)
            prefs.remove(KEY_PENDING_STATE)
        }
    }

    suspend fun saveTokens(token: SpotifyTokenResponse, existingRefresh: String) {
        val refresh = token.refreshToken?.takeIf { it.isNotBlank() } ?: existingRefresh
        val expiresAt = System.currentTimeMillis() + token.expiresInSeconds * 1000L
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token.accessToken
            if (refresh.isNotBlank()) prefs[KEY_REFRESH_TOKEN] = refresh
            prefs[KEY_EXPIRES_AT] = expiresAt
            prefs[KEY_SCOPE] = token.scope
            prefs[KEY_SESSION_EXPIRED] = false
            prefs.remove(KEY_LAST_ERROR)
            prefs.remove(KEY_PENDING_VERIFIER)
            prefs.remove(KEY_PENDING_STATE)
        }
    }

    suspend fun saveProfile(profile: SpotifyProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_PROFILE_ID] = profile.id
            prefs[KEY_DISPLAY_NAME] = profile.displayName
            prefs[KEY_EMAIL] = profile.email.orEmpty()
            prefs[KEY_PRODUCT] = profile.product.orEmpty()
            prefs[KEY_COUNTRY] = profile.country.orEmpty()
            prefs[KEY_PROFILE_IMAGE] = profile.imageUrl.orEmpty()
        }
    }

    suspend fun setAuthError(message: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_ERROR] = message
            prefs.remove(KEY_PENDING_VERIFIER)
            prefs.remove(KEY_PENDING_STATE)
        }
    }

    suspend fun setSessionExpired(expired: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_EXPIRED] = expired
            if (expired) {
                prefs[KEY_LAST_ERROR] = "Spotify session expired. Log in again."
            }
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_EXPIRES_AT)
            prefs.remove(KEY_SCOPE)
            prefs.remove(KEY_PROFILE_ID)
            prefs.remove(KEY_DISPLAY_NAME)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_PRODUCT)
            prefs.remove(KEY_COUNTRY)
            prefs.remove(KEY_PROFILE_IMAGE)
            prefs.remove(KEY_LAST_ERROR)
            prefs[KEY_SESSION_EXPIRED] = false
            prefs.remove(KEY_PENDING_VERIFIER)
            prefs.remove(KEY_PENDING_STATE)
        }
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_EXPIRES_AT = longPreferencesKey("expires_at")
        private val KEY_SCOPE = stringPreferencesKey("scope")
        private val KEY_PENDING_VERIFIER = stringPreferencesKey("pending_verifier")
        private val KEY_PENDING_STATE = stringPreferencesKey("pending_state")
        private val KEY_PROFILE_ID = stringPreferencesKey("profile_id")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PRODUCT = stringPreferencesKey("product")
        private val KEY_COUNTRY = stringPreferencesKey("country")
        private val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image")
        private val KEY_LAST_ERROR = stringPreferencesKey("last_error")
        private val KEY_SESSION_EXPIRED = booleanPreferencesKey("session_expired")
    }
}
