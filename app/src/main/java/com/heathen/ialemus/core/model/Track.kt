package com.heathen.ialemus.core.model

data class Track(
    val id: String,
    val title: String,
    val artist: String? = null,
    val albumArtist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val durationMs: Long = 0L,
    val uri: String,
    val sourceType: SourceType = SourceType.LOCAL,
    val origin: TrackOrigin = TrackOrigin.UNKNOWN,
    val artworkUri: String? = null,
    val lyricsUri: String? = null,
    val dateAdded: Long = 0L,
    val lastScannedAt: Long = 0L,
)

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
