package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathen.ialemus.data.local.entity.LibrarySourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibrarySourceDao {
    @Query("SELECT * FROM library_sources ORDER BY displayName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<LibrarySourceEntity>>

    @Query("SELECT * FROM library_sources WHERE type = 'SAF_FOLDER' ORDER BY displayName COLLATE NOCASE ASC")
    fun observeSafFolders(): Flow<List<LibrarySourceEntity>>

    @Query("SELECT * FROM library_sources WHERE type = 'SAF_FOLDER' ORDER BY displayName COLLATE NOCASE ASC")
    suspend fun getSafFoldersOnce(): List<LibrarySourceEntity>

    @Query("SELECT COUNT(*) FROM library_sources WHERE type = 'SAF_FOLDER'")
    suspend fun countSafFolders(): Int

    @Query("SELECT * FROM library_sources WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LibrarySourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(source: LibrarySourceEntity)

    @Query("DELETE FROM library_sources WHERE id = :id")
    suspend fun deleteById(id: String)
}
