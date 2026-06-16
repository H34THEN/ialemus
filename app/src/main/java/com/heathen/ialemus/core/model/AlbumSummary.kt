package com.heathen.ialemus.core.model

data class AlbumSummary(
    val albumId: Long?,
    val album: String,
    val artist: String,
    val trackCount: Int,
    val totalDurationMs: Long,
)
