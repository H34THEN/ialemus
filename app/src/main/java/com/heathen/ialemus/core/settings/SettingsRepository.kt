package com.heathen.ialemus.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.heathen.ialemus.core.model.ThemeId
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

    suspend fun setTheme(themeId: ThemeId) {
        dataStore.edit { prefs -> prefs[KEY_THEME] = themeId.name }
    }

    suspend fun setDapMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_DAP_MODE] = enabled }
    }

    suspend fun setFullDeviceScanEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_FULL_DEVICE_SCAN] = enabled }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_id")
        private val KEY_DAP_MODE = booleanPreferencesKey("dap_mode")
        private val KEY_FULL_DEVICE_SCAN = booleanPreferencesKey("full_device_scan_enabled")
    }
}
