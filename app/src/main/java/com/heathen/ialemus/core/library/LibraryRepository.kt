package com.heathen.ialemus.core.library

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.heathen.ialemus.core.model.AlbumSummary
import com.heathen.ialemus.core.model.ArtistSummary
import com.heathen.ialemus.core.diagnostics.StabilityDiagnostics
import com.heathen.ialemus.core.model.FolderSummary
import com.heathen.ialemus.core.model.LibrarySource
import com.heathen.ialemus.core.model.LibrarySourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.data.local.dao.LibrarySourceDao
import com.heathen.ialemus.data.local.dao.TrackDao
import com.heathen.ialemus.data.local.dao.TrackOverrideDao
import com.heathen.ialemus.data.local.dao.TrackStatsDao
import com.heathen.ialemus.data.local.defaultStatsEntity
import com.heathen.ialemus.data.local.toEntity
import com.heathen.ialemus.data.local.toLibrarySource
import com.heathen.ialemus.data.local.toTrack
import com.heathen.ialemus.data.local.model.AlbumBrowseRow
import com.heathen.ialemus.data.local.model.ArtistBrowseRow
import com.heathen.ialemus.data.local.model.FolderBrowseRow
import com.heathen.ialemus.data.local.entity.LibrarySourceEntity
import com.heathen.ialemus.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class LibraryRepository(
    private val context: Context,
    private val mediaStoreScanner: MediaStoreScanner,
    private val safFolderScanner: SafFolderScanner,
    private val safAccessHelper: SafAccessHelper,
    private val trackDao: TrackDao,
    private val trackStatsDao: TrackStatsDao,
    private val trackOverrideDao: TrackOverrideDao,
    private val librarySourceDao: LibrarySourceDao,
    private val settingsRepository: SettingsRepository,
) {
    private fun Flow<List<TrackEntity>>.mapWithOverrides(): Flow<List<Track>> =
        combine(this, trackOverrideDao.observeAll()) { entities, overrides ->
            val map = overrides.associateBy { it.trackId }
            entities.map { it.toTrack().withOverrides(map[it.id]) }
        }

    val tracks: Flow<List<Track>> = trackDao.observeAllTracks().mapWithOverrides()

    val trackCount: Flow<Int> = trackDao.countTracks()

    val librarySources: Flow<List<LibrarySource>> = librarySourceDao.observeAll().map { list ->
        list.map { it.toLibrarySource() }
    }

    val safFolderSources: Flow<List<LibrarySource>> = librarySourceDao.observeSafFolders().map { list ->
        list.map { it.toLibrarySource() }
    }

    val artistSummaries: Flow<List<ArtistSummary>> =
        trackDao.observeArtistSummaries().map { rows -> rows.map { it.toArtistSummary() } }

    val albumSummaries: Flow<List<AlbumSummary>> =
        trackDao.observeAlbumSummaries().map { rows -> rows.map { it.toAlbumSummary() } }

    val folderSummaries: Flow<List<FolderSummary>> =
        trackDao.observeFolderSummaries().map { rows -> rows.map { it.toFolderSummary() } }

    val audiobookTracks: Flow<List<Track>> =
        trackDao.observeAudiobookTracks().mapWithOverrides()

    val favoriteTracks: Flow<List<Track>> =
        trackStatsDao.observeFavoriteTracks().mapWithOverrides()

    val recentlyAddedTracks: Flow<List<Track>> =
        trackDao.observeRecentlyAddedTracks().mapWithOverrides()

    val recentlyPlayedTracks: Flow<List<Track>> =
        trackStatsDao.observeRecentlyPlayedTracks().mapWithOverrides()

    val mostPlayedTracks: Flow<List<Track>> =
        trackStatsDao.observeMostPlayedTracks().mapWithOverrides()

    fun tracksForArtist(artist: String): Flow<List<Track>> =
        trackDao.observeTracksForArtist(artist).mapWithOverrides()

    fun tracksForAlbum(album: String, artist: String): Flow<List<Track>> =
        trackDao.observeTracksForAlbum(album, artist).mapWithOverrides()

    fun tracksForFolder(librarySourceId: String, folderPath: String): Flow<List<Track>> =
        trackDao.observeTracksForFolder(librarySourceId, folderPath).mapWithOverrides()

    fun resolvePermissionState(): MediaPermissionState {
        val permission = requiredPermission()
        val status = ContextCompat.checkSelfPermission(context, permission)
        return when {
            status == PackageManager.PERMISSION_GRANTED -> MediaPermissionState.Granted
            else -> MediaPermissionState.NotGranted
        }
    }

    fun requiredPermission(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    suspend fun addSafFolder(treeUri: Uri, displayName: String): LibrarySource {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(treeUri, flags)
        val source = LibrarySource(
            id = UUID.randomUUID().toString(),
            type = LibrarySourceType.SAF_FOLDER,
            displayName = displayName.ifBlank { "Music Folder" },
            treeUri = treeUri.toString(),
            addedAt = System.currentTimeMillis(),
        )
        librarySourceDao.upsert(source.toEntity())
        return source
    }

    suspend fun removeSource(sourceId: String) {
        val source = librarySourceDao.getById(sourceId) ?: return
        if (source.type == LibrarySourceType.SAF_FOLDER.name) {
            try {
                val uri = Uri.parse(source.treeUri)
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.releasePersistableUriPermission(uri, flags)
            } catch (_: SecurityException) {
                // Permission may already be revoked.
            }
        }
        trackDao.deleteTracksForSource(sourceId)
        librarySourceDao.deleteById(sourceId)
    }

    suspend fun scanSelectedFolders(): LibraryScanResult {
        val sources = librarySourceDao.getSafFoldersOnce().map { it.toLibrarySource() }
        if (sources.isEmpty()) {
            return LibraryScanResult.Error("No music folders selected. Choose a folder first.")
        }
        return scanSources(sources)
    }

    suspend fun scanPrimaryFolder(): LibraryScanResult {
        val source = librarySourceDao.getSafFoldersOnce().firstOrNull()?.toLibrarySource()
            ?: return LibraryScanResult.Error("No music folders selected. Choose a folder first.")
        return scanSources(listOf(source))
    }

    private suspend fun scanSources(sources: List<LibrarySource>): LibraryScanResult {
        return try {
            StabilityDiagnostics.scanStart(sources.size)
            val scanStartedAt = System.currentTimeMillis()
            safAccessHelper.restorePersistedFolderPermissions(sources)
            var total = 0
            val warnings = mutableListOf<String>()
            sources.forEach { source ->
                if (!safAccessHelper.canReadSource(source)) {
                    val kept = trackDao.countTracksForSource(source.id)
                    warnings += if (kept > 0) {
                        "${source.displayName}: folder not reachable — kept $kept indexed tracks"
                    } else {
                        "${source.displayName}: folder not reachable — reselect folder in Library"
                    }
                    return@forEach
                }
                val scanned = safFolderScanner.scanSource(source)
                if (scanned.isEmpty()) {
                    val kept = trackDao.countTracksForSource(source.id)
                    if (safAccessHelper.isFolderConfirmedEmpty(source)) {
                        trackDao.deleteTracksForSource(source.id)
                        if (kept > 0) {
                            warnings += "${source.displayName}: folder empty — removed $kept stale entries"
                        }
                    } else if (kept > 0) {
                        warnings += "${source.displayName}: scan found no files — kept $kept indexed tracks"
                    } else {
                        warnings += "${source.displayName}: no audio files found"
                    }
                    return@forEach
                }
                trackDao.upsertTracks(scanned.map { it.toEntity() })
                scanned.forEach { track -> trackStatsDao.insertIfAbsent(defaultStatsEntity(track.id)) }
                trackDao.deleteMissingTracksForSource(scanned.map { it.id }, source.id)
                total += scanned.size
            }
            val label = if (sources.size == 1) sources.first().displayName else "Selected folders"
            StabilityDiagnostics.scanEnd(total, System.currentTimeMillis() - scanStartedAt)
            LibraryScanResult.Success(total, label, warnings)
        } catch (error: Exception) {
            LibraryScanResult.Error(error.message ?: "Folder scan failed.")
        }
    }

    suspend fun restorePersistedLibraryAccess() {
        val sources = librarySourceDao.getSafFoldersOnce().map { it.toLibrarySource() }
        safAccessHelper.restorePersistedFolderPermissions(sources)
    }

    suspend fun scanFullDeviceLibrary(): LibraryScanResult {
        if (resolvePermissionState() != MediaPermissionState.Granted) {
            return LibraryScanResult.Error("Full device scan requires music access permission.")
        }
        return try {
            settingsRepository.setFullDeviceScanEnabled(true)
            val scanned = mediaStoreScanner.scanFullDevice()
            if (scanned.isEmpty()) {
                val kept = trackDao.countTracksOnce()
                return if (kept > 0) {
                    LibraryScanResult.Success(
                        trackCount = kept,
                        sourceLabel = "Full device library",
                        warnings = listOf("Full device scan found no new files — kept $kept indexed tracks"),
                    )
                } else {
                    LibraryScanResult.Error("Full device scan found no audio files.")
                }
            }
            trackDao.upsertTracks(scanned.map { it.toEntity() })
            scanned.forEach { track -> trackStatsDao.insertIfAbsent(defaultStatsEntity(track.id)) }
            trackDao.deleteMissingFullDeviceTracks(scanned.map { it.id })
            LibraryScanResult.Success(scanned.size, "Full device library")
        } catch (error: Exception) {
            LibraryScanResult.Error(error.message ?: "Full device scan failed.")
        }
    }

    suspend fun getTrackById(id: String): Track? {
        val entity = trackDao.getTrackById(id) ?: return null
        val override = trackOverrideDao.getForTrack(id)
        return entity.toTrack().withOverrides(override)
    }

    suspend fun refreshScanStateHint(): LibraryScanState {
        val folderCount = librarySourceDao.countSafFolders()
        val tracks = trackDao.countTracksOnce()
        return when {
            folderCount == 0 && tracks == 0 -> LibraryScanState.NoSources
            folderCount > 0 && tracks == 0 -> LibraryScanState.FoldersSelected(folderCount)
            folderCount > 0 -> LibraryScanState.FolderScanComplete(tracks)
            else -> LibraryScanState.FullDeviceAvailable
        }
    }
}

private fun LibrarySource.toEntity() = LibrarySourceEntity(
    id = id,
    type = type.name,
    displayName = displayName,
    treeUri = treeUri,
    addedAt = addedAt,
)

private fun ArtistBrowseRow.toArtistSummary() = ArtistSummary(
    artist = artistKey,
    trackCount = trackCount,
    albumCount = albumCount,
)

private fun AlbumBrowseRow.toAlbumSummary() = AlbumSummary(
    albumId = albumId,
    album = albumKey,
    artist = artistKey,
    trackCount = trackCount,
    totalDurationMs = totalDurationMs,
)

private fun FolderBrowseRow.toFolderSummary() = FolderSummary(
    librarySourceId = librarySourceId,
    sourceLabel = sourceLabel,
    folderPath = folderPath,
    trackCount = trackCount,
)
