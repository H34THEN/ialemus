package com.heathen.ialemus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library_sources")
data class LibrarySourceEntity(
    @PrimaryKey val id: String,
    val type: String,
    val displayName: String,
    val treeUri: String,
    val addedAt: Long,
)
