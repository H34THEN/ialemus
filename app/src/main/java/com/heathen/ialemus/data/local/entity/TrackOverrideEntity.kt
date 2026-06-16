package com.heathen.ialemus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_overrides")
data class TrackOverrideEntity(
    @PrimaryKey val trackId: String,
    val displayTitleOverride: String? = null,
    val displayArtistOverride: String? = null,
    val displayAlbumOverride: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)
