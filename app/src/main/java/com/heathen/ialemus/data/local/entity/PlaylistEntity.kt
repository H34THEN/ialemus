package com.heathen.ialemus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sourceType: String = SOURCE_MANUAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_M3U = "m3u"
    }
}
