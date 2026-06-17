package com.heathen.ialemus.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.heathen.ialemus.core.model.ConnectionMode
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.model.NowPlayingVisualizerMode
import com.heathen.ialemus.core.model.ThemeId
import com.heathen.ialemus.core.spotify.SpotifyDefaults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ialemus_settings")

class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    val themeId: Flow<ThemeId> = dataStore.data.map { prefs ->
        ThemeId.entries.find { it.name == prefs[KEY_THEME] } ?: ThemeId.DEFAULT
    }

    val dapModeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DAP_MODE] ?: false
    }

    val fullDeviceScanEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_FULL_DEVICE_SCAN] ?: false
    }

    val showMiniPlayerBar: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SHOW_MINI_PLAYER] ?: true
    }

    val nowPlayingLayoutMode: Flow<NowPlayingLayoutMode> = dataStore.data.map { prefs ->
        NowPlayingLayoutMode.entries.find { it.name == prefs[KEY_NOW_PLAYING_LAYOUT] }
            ?: NowPlayingLayoutMode.BALANCED
    }

    val nowPlayingVisualizerMode: Flow<NowPlayingVisualizerMode> = dataStore.data.map { prefs ->
        NowPlayingVisualizerMode.entries.find { it.name == prefs[KEY_NOW_PLAYING_VISUALIZER] }
            ?: NowPlayingVisualizerMode.SIGNAL_BARS
    }

    val spotifySettings: Flow<SpotifySettings> = dataStore.data.map { prefs ->
        SpotifySettings(
            clientId = prefs[KEY_SPOTIFY_CLIENT_ID].orEmpty(),
            displayName = prefs[KEY_SPOTIFY_DISPLAY_NAME].orEmpty(),
            connected = prefs[KEY_SPOTIFY_CONNECTED] ?: false,
        )
    }

    val nasConnectionSettings: Flow<NasConnectionSettings> = dataStore.data.map { prefs ->
        NasConnectionSettings(
            nasDisplayName = prefs[KEY_NAS_DISPLAY_NAME].orEmpty(),
            bridgeUrl = prefs[KEY_BRIDGE_URL].orEmpty(),
            bridgeToken = prefs[KEY_BRIDGE_TOKEN].orEmpty(),
            meTubeUrl = prefs[KEY_METUBE_URL].orEmpty(),
            slskdUrl = prefs[KEY_SLSKD_URL].orEmpty(),
            nasUiUrl = prefs[KEY_NAS_UI_URL].orEmpty().ifBlank {
                prefs[KEY_JELLYFIN_URL].orEmpty()
            },
            connectionMode = ConnectionMode.entries.find {
                it.name == prefs[KEY_CONNECTION_MODE]
            } ?: ConnectionMode.LOCAL_LAN,
        )
    }

    suspend fun setTheme(themeId: ThemeId) {
        dataStore.edit { prefs -> prefs[KEY_THEME] = themeId.name }
    }

    suspend fun setDapMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_DAP_MODE] = enabled }
    }

    suspend fun setFullDeviceScanEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_FULL_DEVICE_SCAN] = enabled }
    }

    suspend fun setShowMiniPlayerBar(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_SHOW_MINI_PLAYER] = enabled }
    }

    suspend fun setNowPlayingLayoutMode(mode: NowPlayingLayoutMode) {
        dataStore.edit { prefs -> prefs[KEY_NOW_PLAYING_LAYOUT] = mode.name }
    }

    suspend fun setNowPlayingVisualizerMode(mode: NowPlayingVisualizerMode) {
        dataStore.edit { prefs -> prefs[KEY_NOW_PLAYING_VISUALIZER] = mode.name }
    }

    suspend fun saveSpotifySettings(settings: SpotifySettings) {
        dataStore.edit { prefs ->
            if (settings.clientId.isBlank()) {
                prefs.remove(KEY_SPOTIFY_CLIENT_ID)
            } else {
                prefs[KEY_SPOTIFY_CLIENT_ID] = settings.clientId.trim()
            }
            prefs[KEY_SPOTIFY_DISPLAY_NAME] = settings.displayName.trim()
            prefs[KEY_SPOTIFY_CONNECTED] = settings.connected
        }
    }

    suspend fun setSpotifyConnected(connected: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_SPOTIFY_CONNECTED] = connected }
    }

    suspend fun saveNasConnectionSettings(settings: NasConnectionSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_NAS_DISPLAY_NAME] = settings.nasDisplayName.trim()
            prefs[KEY_BRIDGE_URL] = settings.bridgeUrl.trim()
            prefs[KEY_BRIDGE_TOKEN] = settings.bridgeToken.trim()
            prefs[KEY_METUBE_URL] = settings.meTubeUrl.trim()
            prefs[KEY_SLSKD_URL] = settings.slskdUrl.trim()
            prefs[KEY_NAS_UI_URL] = settings.nasUiUrl.trim()
            prefs[KEY_CONNECTION_MODE] = settings.connectionMode.name
        }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_id")
        private val KEY_DAP_MODE = booleanPreferencesKey("dap_mode")
        private val KEY_FULL_DEVICE_SCAN = booleanPreferencesKey("full_device_scan_enabled")
        private val KEY_SHOW_MINI_PLAYER = booleanPreferencesKey("show_mini_player_bar")
        private val KEY_NOW_PLAYING_LAYOUT = stringPreferencesKey("now_playing_layout_mode")
        private val KEY_NOW_PLAYING_VISUALIZER = stringPreferencesKey("now_playing_visualizer_mode")
        private val KEY_SPOTIFY_CLIENT_ID = stringPreferencesKey("spotify_client_id")
        private val KEY_SPOTIFY_DISPLAY_NAME = stringPreferencesKey("spotify_display_name")
        private val KEY_SPOTIFY_CONNECTED = booleanPreferencesKey("spotify_connected")
        private val KEY_NAS_DISPLAY_NAME = stringPreferencesKey("nas_display_name")
        private val KEY_BRIDGE_URL = stringPreferencesKey("bridge_url")
        private val KEY_BRIDGE_TOKEN = stringPreferencesKey("bridge_token")
        private val KEY_METUBE_URL = stringPreferencesKey("metube_url")
        private val KEY_SLSKD_URL = stringPreferencesKey("slskd_url")
        private val KEY_NAS_UI_URL = stringPreferencesKey("nas_ui_url")
        private val KEY_JELLYFIN_URL = stringPreferencesKey("jellyfin_url")
        private val KEY_CONNECTION_MODE = stringPreferencesKey("connection_mode")
    }
}
