package com.heathen.ialemus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val trackId: String,
    val sourceType: String,
    val rawText: String,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class LyricsSourceType {
    MANUAL,
    LRC_SIDECAR,
    TXT_SIDECAR,
    EMBEDDED,
    FUTURE_PROVIDER,
}
