package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.Track

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queueIndex: Int = -1,
    val queueSize: Int = 0,
    val isConnected: Boolean = false,
)
