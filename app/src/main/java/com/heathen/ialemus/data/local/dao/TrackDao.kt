package com.heathen.ialemus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathen.ialemus.data.local.entity.TrackEntity
import com.heathen.ialemus.data.local.model.AlbumBrowseRow
import com.heathen.ialemus.data.local.model.ArtistBrowseRow
import com.heathen.ialemus.data.local.model.FolderBrowseRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM tracks")
    fun countTracks(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun countTracksOnce(): Int

    @Query("SELECT COUNT(*) FROM tracks WHERE librarySourceId = :sourceId")
    suspend fun countTracksForSource(sourceId: String): Int

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE librarySourceId = :sourceId")
    suspend fun deleteTracksForSource(sourceId: String)

    @Query("DELETE FROM tracks WHERE sourceType = 'MEDIASTORE_FULL_DEVICE' OR sourceType = 'LOCAL'")
    suspend fun deleteFullDeviceTracks()

    @Query("DELETE FROM tracks WHERE id NOT IN (:ids) AND librarySourceId = :sourceId")
    suspend fun deleteMissingTracksForSource(ids: List<String>, sourceId: String)

    @Query("DELETE FROM tracks WHERE id NOT IN (:ids) AND (sourceType = 'MEDIASTORE_FULL_DEVICE' OR sourceType = 'LOCAL')")
    suspend fun deleteMissingFullDeviceTracks(ids: List<String>)

    @Query(
        """
        SELECT
            COALESCE(NULLIF(TRIM(artist), ''), 'Unknown Artist') AS artistKey,
            COUNT(*) AS trackCount,
            COUNT(DISTINCT COALESCE(NULLIF(TRIM(album), ''), 'Unknown Album')) AS albumCount
        FROM tracks
        GROUP BY artistKey
        ORDER BY artistKey COLLATE NOCASE ASC
        """,
    )
    fun observeArtistSummaries(): Flow<List<ArtistBrowseRow>>

    @Query(
        """
        SELECT
            COALESCE(NULLIF(TRIM(album), ''), 'Unknown Album') AS albumKey,
            COALESCE(NULLIF(TRIM(artist), ''), 'Unknown Artist') AS artistKey,
            MAX(albumId) AS albumId,
            COUNT(*) AS trackCount,
            SUM(durationMs) AS totalDurationMs
        FROM tracks
        GROUP BY albumKey, artistKey
        ORDER BY albumKey COLLATE NOCASE ASC
        """,
    )
    fun observeAlbumSummaries(): Flow<List<AlbumBrowseRow>>

    @Query(
        """
        SELECT
            librarySourceId,
            COALESCE(NULLIF(sourceLabel, ''), 'Music Folder') AS sourceLabel,
            COALESCE(NULLIF(relativePath, ''), '/') AS folderPath,
            COUNT(*) AS trackCount
        FROM tracks
        WHERE librarySourceId IS NOT NULL
        GROUP BY librarySourceId, folderPath
        ORDER BY sourceLabel COLLATE NOCASE ASC, folderPath COLLATE NOCASE ASC
        """,
    )
    fun observeFolderSummaries(): Flow<List<FolderBrowseRow>>

    @Query(
        """
        SELECT * FROM tracks
        WHERE COALESCE(NULLIF(TRIM(artist), ''), 'Unknown Artist') = :artist
        ORDER BY album COLLATE NOCASE ASC, trackNumber ASC, title COLLATE NOCASE ASC
        """,
    )
    fun observeTracksForArtist(artist: String): Flow<List<TrackEntity>>

    @Query(
        """
        SELECT * FROM tracks
        WHERE COALESCE(NULLIF(TRIM(album), ''), 'Unknown Album') = :album
          AND COALESCE(NULLIF(TRIM(artist), ''), 'Unknown Artist') = :artist
        ORDER BY discNumber ASC, trackNumber ASC, title COLLATE NOCASE ASC
        """,
    )
    fun observeTracksForAlbum(album: String, artist: String): Flow<List<TrackEntity>>

    @Query(
        """
        SELECT * FROM tracks
        WHERE librarySourceId = :librarySourceId
          AND COALESCE(NULLIF(relativePath, ''), '/') = :folderPath
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeTracksForFolder(librarySourceId: String, folderPath: String): Flow<List<TrackEntity>>

    @Query(
        """
        SELECT * FROM tracks
        WHERE durationMs >= :minDurationMs
           OR LOWER(COALESCE(relativePath, '')) LIKE '%audiobook%'
           OR LOWER(COALESCE(relativePath, '')) LIKE '%audiobooks%'
           OR LOWER(COALESCE(relativePath, '')) LIKE '%/books/%'
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeAudiobookTracks(minDurationMs: Long = 1_200_000L): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT :limit")
    fun observeRecentlyAddedTracks(limit: Int = 50): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllOnce(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<TrackEntity>
}
