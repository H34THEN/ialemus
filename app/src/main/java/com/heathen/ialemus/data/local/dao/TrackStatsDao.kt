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
}
