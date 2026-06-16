package com.heathen.ialemus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_stats")
data class TrackStatsEntity(
    @PrimaryKey val trackId: String,
    val playCount: Int = 0,
    val skipCount: Int = 0,
    val completionCount: Int = 0,
    val totalListenTimeMs: Long = 0L,
    val favorite: Boolean = false,
    val firstPlayedAt: Long? = null,
    val lastPlayedAt: Long? = null,
)
