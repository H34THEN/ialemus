package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathen.ialemus.data.local.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    fun observeByTrackId(trackId: String): Flow<LyricsEntity?>

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    suspend fun getByTrackId(trackId: String): LyricsEntity?

    @Query("SELECT trackId FROM lyrics")
    fun observeTrackIdsWithLyrics(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: String)
}
