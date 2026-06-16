package com.heathen.ialemus.core.model

data class Track(
    val id: String,
    val mediaStoreId: Long,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val albumId: Long? = null,
    val durationMs: Long = 0L,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val contentUri: String,
    val relativePath: String? = null,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val size: Long? = null,
    val sourceType: SourceType = SourceType.LOCAL,
    val origin: TrackOrigin = TrackOrigin.MANUAL,
    val lastScannedAt: Long = 0L,
) {
    val displayArtist: String
        get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"

    val displayAlbum: String
        get() = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
}

enum class SourceType {
    LOCAL,
    NAS_INDEXED,
    NAS_STREAM,
    DEVICE_CACHE,
}

enum class TrackOrigin {
    MANUAL,
    SPOTDL,
    METUBE,
    SLSKD,
    JELLYFIN,
    UNKNOWN,
}

fun localTrackId(mediaStoreId: Long): String = "local_$mediaStoreId"
