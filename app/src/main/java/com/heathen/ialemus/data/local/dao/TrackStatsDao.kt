package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathen.ialemus.data.local.entity.TrackStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackStatsDao {
    @Query("SELECT * FROM track_stats WHERE trackId = :trackId LIMIT 1")
    fun observeStatsForTrack(trackId: String): Flow<TrackStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: TrackStatsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(stats: TrackStatsEntity)

    @Query("UPDATE track_stats SET favorite = :favorite WHERE trackId = :trackId")
    suspend fun setFavorite(trackId: String, favorite: Boolean)

    @Query(
        """
        UPDATE track_stats
        SET playCount = playCount + 1, lastPlayedAt = :playedAt
        WHERE trackId = :trackId
        """,
    )
    suspend fun incrementPlayCount(trackId: String, playedAt: Long)

    @Query(
        """
        SELECT tracks.* FROM tracks
        INNER JOIN track_stats ON tracks.id = track_stats.trackId
        WHERE track_stats.favorite = 1
        ORDER BY tracks.title COLLATE NOCASE ASC
        """,
    )
    fun observeFavoriteTracks(): Flow<List<com.heathen.ialemus.data.local.entity.TrackEntity>>

    @Query(
        """
        SELECT tracks.* FROM tracks
        INNER JOIN track_stats ON tracks.id = track_stats.trackId
        WHERE track_stats.lastPlayedAt IS NOT NULL AND track_stats.lastPlayedAt > 0
        ORDER BY track_stats.lastPlayedAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecentlyPlayedTracks(limit: Int = 50): Flow<List<com.heathen.ialemus.data.local.entity.TrackEntity>>

    @Query(
        """
        SELECT tracks.* FROM tracks
        INNER JOIN track_stats ON tracks.id = track_stats.trackId
        WHERE track_stats.playCount > 0
        ORDER BY track_stats.playCount DESC, track_stats.lastPlayedAt DESC
        LIMIT :limit
        """,
    )
    fun observeMostPlayedTracks(limit: Int = 50): Flow<List<com.heathen.ialemus.data.local.entity.TrackEntity>>
}
