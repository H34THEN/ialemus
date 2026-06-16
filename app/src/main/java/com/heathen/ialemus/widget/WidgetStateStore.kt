package com.heathen.ialemus.widget

import android.content.Context

data class WidgetPlaybackSnapshot(
    val title: String = "No track loaded",
    val artist: String = "Standby",
    val isPlaying: Boolean = false,
)

class WidgetStateStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun update(title: String?, artist: String?, isPlaying: Boolean) {
        prefs.edit()
            .putString(KEY_TITLE, title?.takeIf { it.isNotBlank() } ?: "No track loaded")
            .putString(KEY_ARTIST, artist?.takeIf { it.isNotBlank() } ?: "Standby")
            .putBoolean(KEY_PLAYING, isPlaying)
            .apply()
    }

    fun read(): WidgetPlaybackSnapshot = WidgetPlaybackSnapshot(
        title = prefs.getString(KEY_TITLE, "No track loaded") ?: "No track loaded",
        artist = prefs.getString(KEY_ARTIST, "Standby") ?: "Standby",
        isPlaying = prefs.getBoolean(KEY_PLAYING, false),
    )

    companion object {
        private const val PREFS_NAME = "ialemus_widget_state"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST = "artist"
        private const val KEY_PLAYING = "playing"
    }
}
