package com.heathen.ialemus.core.library

import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.data.local.dao.TrackOverrideDao
import com.heathen.ialemus.data.local.entity.TrackOverrideEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrackOverrideRepository(
    private val trackOverrideDao: TrackOverrideDao,
) {
    val overrides: Flow<Map<String, TrackOverrideEntity>> = trackOverrideDao.observeAll().map { list ->
        list.associateBy { it.trackId }
    }

    fun observeForTrack(trackId: String): Flow<TrackOverrideEntity?> =
        trackOverrideDao.observeForTrack(trackId)

    suspend fun saveTitleOverride(trackId: String, displayTitle: String) {
        val existing = trackOverrideDao.getForTrack(trackId)
        trackOverrideDao.upsert(
            (existing ?: TrackOverrideEntity(trackId = trackId)).copy(
                displayTitleOverride = displayTitle.trim(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun saveArtistOverride(trackId: String, displayArtist: String) {
        val existing = trackOverrideDao.getForTrack(trackId)
        trackOverrideDao.upsert(
            (existing ?: TrackOverrideEntity(trackId = trackId)).copy(
                displayArtistOverride = displayArtist.trim(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun saveAlbumOverride(trackId: String, displayAlbum: String) {
        val existing = trackOverrideDao.getForTrack(trackId)
        trackOverrideDao.upsert(
            (existing ?: TrackOverrideEntity(trackId = trackId)).copy(
                displayAlbumOverride = displayAlbum.trim(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun saveAllOverrides(
        trackId: String,
        displayTitle: String?,
        displayArtist: String?,
        displayAlbum: String?,
    ) {
        val existing = trackOverrideDao.getForTrack(trackId)
        trackOverrideDao.upsert(
            (existing ?: TrackOverrideEntity(trackId = trackId)).copy(
                displayTitleOverride = displayTitle?.trim()?.ifBlank { null },
                displayArtistOverride = displayArtist?.trim()?.ifBlank { null },
                displayAlbumOverride = displayAlbum?.trim()?.ifBlank { null },
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun clearOverride(trackId: String) {
        trackOverrideDao.deleteForTrack(trackId)
    }
}

fun Track.withOverrides(override: TrackOverrideEntity?): Track = copy(
    displayTitleOverride = override?.displayTitleOverride,
    displayArtistOverride = override?.displayArtistOverride,
    displayAlbumOverride = override?.displayAlbumOverride,
)

fun List<Track>.withOverrides(overrideMap: Map<String, TrackOverrideEntity>): List<Track> =
    map { track -> track.withOverrides(overrideMap[track.id]) }
