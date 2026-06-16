package com.heathen.ialemus.core.model

data class TrackStats(
    val trackId: String,
    val playCount: Int = 0,
    val skipCount: Int = 0,
    val completionCount: Int = 0,
    val totalListenTimeMs: Long = 0L,
    val favorite: Boolean = false,
    val firstPlayedAt: Long? = null,
    val lastPlayedAt: Long? = null,
)
