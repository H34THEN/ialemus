package com.heathen.ialemus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val mediaStoreId: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val durationMs: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val contentUri: String,
    val relativePath: String?,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long?,
    val sourceType: String,
    val origin: String,
    val lastScannedAt: Long,
)
