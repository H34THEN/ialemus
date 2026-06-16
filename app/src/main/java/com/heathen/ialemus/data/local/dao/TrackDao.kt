package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathen.ialemus.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM tracks")
    fun countTracks(): Flow<Int>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE id NOT IN (:ids)")
    suspend fun deleteMissingTracks(ids: List<String>)
}
