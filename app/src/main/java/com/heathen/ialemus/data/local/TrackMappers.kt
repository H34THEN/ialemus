package com.heathen.ialemus.data.local

import com.heathen.ialemus.core.model.SourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.model.TrackOrigin
import com.heathen.ialemus.core.model.TrackStats
import com.heathen.ialemus.data.local.entity.TrackEntity
import com.heathen.ialemus.data.local.entity.TrackStatsEntity

fun TrackEntity.toTrack(): Track = Track(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    durationMs = durationMs,
    trackNumber = trackNumber,
    discNumber = discNumber,
    contentUri = contentUri,
    relativePath = relativePath,
    dateAdded = dateAdded,
    dateModified = dateModified,
    size = size,
    sourceType = SourceType.valueOf(sourceType),
    origin = TrackOrigin.valueOf(origin),
    lastScannedAt = lastScannedAt,
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    durationMs = durationMs,
    trackNumber = trackNumber,
    discNumber = discNumber,
    contentUri = contentUri,
    relativePath = relativePath,
    dateAdded = dateAdded,
    dateModified = dateModified,
    size = size,
    sourceType = sourceType.name,
    origin = origin.name,
    lastScannedAt = lastScannedAt,
)

fun TrackStatsEntity.toTrackStats(): TrackStats = TrackStats(
    trackId = trackId,
    playCount = playCount,
    skipCount = skipCount,
    completionCount = completionCount,
    totalListenTimeMs = totalListenTimeMs,
    favorite = favorite,
    firstPlayedAt = firstPlayedAt,
    lastPlayedAt = lastPlayedAt,
)

fun defaultStatsEntity(trackId: String): TrackStatsEntity = TrackStatsEntity(trackId = trackId)
