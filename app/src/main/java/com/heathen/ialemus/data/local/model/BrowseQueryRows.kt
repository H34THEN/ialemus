package com.heathen.ialemus.data.local.model

data class ArtistBrowseRow(
    val artistKey: String,
    val trackCount: Int,
    val albumCount: Int,
)

data class AlbumBrowseRow(
    val albumKey: String,
    val artistKey: String,
    val albumId: Long?,
    val trackCount: Int,
    val totalDurationMs: Long,
)

data class FolderBrowseRow(
    val librarySourceId: String,
    val sourceLabel: String,
    val folderPath: String,
    val trackCount: Int,
)
