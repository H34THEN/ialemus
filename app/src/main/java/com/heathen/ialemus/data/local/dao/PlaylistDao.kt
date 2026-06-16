package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.heathen.ialemus.data.local.entity.PlaylistEntity
import com.heathen.ialemus.data.local.entity.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: String,
    val name: String,
    val sourceType: String,
    val createdAt: Long,
    val updatedAt: Long,
    val trackCount: Int,
)

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT p.id, p.name, p.sourceType, p.createdAt, p.updatedAt,
               COUNT(pt.trackId) AS trackCount
        FROM playlists p
        LEFT JOIN playlist_tracks pt ON pt.playlistId = p.id
        GROUP BY p.id
        ORDER BY p.updatedAt DESC
        """,
    )
    fun observePlaylistsWithCount(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observeTrackIds(playlistId: String): Flow<List<String>>

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getTrackIds(playlistId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(entries: List<PlaylistTrackEntity>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrack(playlistId: String, trackId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearTracks(playlistId: String)

    @Transaction
    suspend fun replaceTracks(playlistId: String, entries: List<PlaylistTrackEntity>) {
        clearTracks(playlistId)
        if (entries.isNotEmpty()) upsertTracks(entries)
    }
}
