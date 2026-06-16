package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathen.ialemus.data.local.entity.TrackOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackOverrideDao {
    @Query("SELECT * FROM track_overrides")
    fun observeAll(): Flow<List<TrackOverrideEntity>>

    @Query("SELECT * FROM track_overrides WHERE trackId = :trackId LIMIT 1")
    fun observeForTrack(trackId: String): Flow<TrackOverrideEntity?>

    @Query("SELECT * FROM track_overrides WHERE trackId = :trackId LIMIT 1")
    suspend fun getForTrack(trackId: String): TrackOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TrackOverrideEntity)

    @Query("DELETE FROM track_overrides WHERE trackId = :trackId")
    suspend fun deleteForTrack(trackId: String)
}
