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
    val sourceType: SourceType = SourceType.SAF_FOLDER,
    val origin: TrackOrigin = TrackOrigin.MANUAL,
    val librarySourceId: String? = null,
    val sourceLabel: String? = null,
    val lastScannedAt: Long = 0L,
) {
    val displayArtist: String
        get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"

    val displayAlbum: String
        get() = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"

    val sourceChipLabel: String
        get() = when (sourceType) {
            SourceType.SAF_FOLDER -> "LOCAL FOLDER"
            SourceType.MEDIASTORE_FULL_DEVICE, SourceType.LOCAL -> "FULL SCAN"
            SourceType.FUTURE_NAS, SourceType.NAS_INDEXED, SourceType.NAS_STREAM -> "NAS"
            SourceType.DEVICE_CACHE -> "CACHE"
        }
}

enum class SourceType {
    SAF_FOLDER,
    MEDIASTORE_FULL_DEVICE,
    LOCAL,
    NAS_INDEXED,
    NAS_STREAM,
    DEVICE_CACHE,
    FUTURE_NAS,
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

fun safTrackId(sourceId: String, documentUri: String): String =
    "saf_${sourceId}_${documentUri.hashCode()}"
