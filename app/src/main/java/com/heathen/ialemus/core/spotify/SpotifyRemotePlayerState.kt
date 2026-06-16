package com.heathen.ialemus.core.spotify

data class SpotifyRemotePlayerState(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val imageUri: String?,
    val isPaused: Boolean,
    val playbackPositionMs: Long?,
    val trackDurationMs: Long?,
) {
    val isPlaying: Boolean get() = !isPaused
}
