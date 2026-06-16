package com.heathen.ialemus.core.playlist

import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.data.local.dao.PlaylistDao
import com.heathen.ialemus.data.local.toTrack
import com.heathen.ialemus.data.local.dao.PlaylistWithCount
import com.heathen.ialemus.data.local.dao.TrackDao
import com.heathen.ialemus.data.local.entity.PlaylistEntity
import com.heathen.ialemus.data.local.entity.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class PlaylistSummary(
    val id: String,
    val name: String,
    val sourceType: String,
    val trackCount: Int,
    val updatedAt: Long,
)

data class M3uImportResult(
    val playlistId: String,
    val playlistName: String,
    val matchedCount: Int,
    val unmatchedEntries: List<String>,
)

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao,
) {
    val playlists: Flow<List<PlaylistSummary>> = playlistDao.observePlaylistsWithCount().map { rows ->
        rows.map { it.toSummary() }
    }

    fun observeTrackIds(playlistId: String): Flow<List<String>> =
        playlistDao.observeTrackIds(playlistId)

    suspend fun createPlaylist(name: String): PlaylistSummary {
        val trimmed = name.trim().ifBlank { "New Playlist" }
        val now = System.currentTimeMillis()
        val entity = PlaylistEntity(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            updatedAt = now,
            createdAt = now,
        )
        playlistDao.upsertPlaylist(entity)
        return entity.toSummary(trackCount = 0)
    }

    suspend fun renamePlaylist(playlistId: String, name: String) {
        val existing = playlistDao.getById(playlistId) ?: return
        playlistDao.upsertPlaylist(
            existing.copy(
                name = name.trim().ifBlank { existing.name },
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addTrack(playlistId: String, trackId: String) {
        val existing = playlistDao.getTrackIds(playlistId)
        if (trackId in existing) return
        val position = existing.size
        playlistDao.upsertTracks(
            listOf(PlaylistTrackEntity(playlistId = playlistId, trackId = trackId, position = position)),
        )
        touchPlaylist(playlistId)
    }

    suspend fun removeTrack(playlistId: String, trackId: String) {
        playlistDao.removeTrack(playlistId, trackId)
        val remaining = playlistDao.getTrackIds(playlistId)
        playlistDao.replaceTracks(
            playlistId,
            remaining.mapIndexed { index, id ->
                PlaylistTrackEntity(playlistId = playlistId, trackId = id, position = index)
            },
        )
        touchPlaylist(playlistId)
    }

    suspend fun getTracksForPlaylist(playlistId: String): List<Track> {
        val ids = playlistDao.getTrackIds(playlistId)
        if (ids.isEmpty()) return emptyList()
        val tracks = trackDao.getByIds(ids).associateBy { it.id }
        return ids.mapNotNull { id -> tracks[id]?.toTrack() }
    }

    suspend fun importM3u(name: String, lines: List<String>): M3uImportResult {
        val playlist = createPlaylist(name)
        val allTracks = trackDao.getAllOnce().map { it.toTrack() }
        val matched = mutableListOf<String>()
        val unmatched = mutableListOf<String>()
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach
            val path = line.substringAfterLast('/').substringAfterLast('\\')
            val match = allTracks.firstOrNull { track ->
                track.contentUri == line ||
                    track.relativePath == line ||
                    track.relativePath?.endsWith(line.trimStart('/')) == true ||
                    track.title.equals(path, ignoreCase = true) ||
                    track.displayTitle.equals(path.substringBeforeLast('.'), ignoreCase = true) ||
                    track.contentUri.endsWith(path, ignoreCase = true)
            }
            if (match != null) {
                matched += match.id
            } else {
                unmatched += line
            }
        }
        playlistDao.replaceTracks(
            playlist.id,
            matched.distinct().mapIndexed { index, trackId ->
                PlaylistTrackEntity(playlistId = playlist.id, trackId = trackId, position = index)
            },
        )
        touchPlaylist(playlist.id)
        return M3uImportResult(
            playlistId = playlist.id,
            playlistName = playlist.name,
            matchedCount = matched.distinct().size,
            unmatchedEntries = unmatched,
        )
    }

    private suspend fun touchPlaylist(playlistId: String) {
        val existing = playlistDao.getById(playlistId) ?: return
        playlistDao.upsertPlaylist(existing.copy(updatedAt = System.currentTimeMillis()))
    }

    private fun PlaylistWithCount.toSummary() = PlaylistSummary(
        id = id,
        name = name,
        sourceType = sourceType,
        trackCount = trackCount,
        updatedAt = updatedAt,
    )

    private fun PlaylistEntity.toSummary(trackCount: Int) = PlaylistSummary(
        id = id,
        name = name,
        sourceType = sourceType,
        trackCount = trackCount,
        updatedAt = updatedAt,
    )
}
