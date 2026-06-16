package com.heathen.ialemus.core.library

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.heathen.ialemus.core.model.LibrarySource
import com.heathen.ialemus.core.model.LibrarySourceType
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.data.local.dao.LibrarySourceDao
import com.heathen.ialemus.data.local.dao.TrackDao
import com.heathen.ialemus.data.local.dao.TrackStatsDao
import com.heathen.ialemus.data.local.defaultStatsEntity
import com.heathen.ialemus.data.local.toEntity
import com.heathen.ialemus.data.local.toLibrarySource
import com.heathen.ialemus.data.local.toTrack
import com.heathen.ialemus.data.local.entity.LibrarySourceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class LibraryRepository(
    private val context: Context,
    private val mediaStoreScanner: MediaStoreScanner,
    private val safFolderScanner: SafFolderScanner,
    private val trackDao: TrackDao,
    private val trackStatsDao: TrackStatsDao,
    private val librarySourceDao: LibrarySourceDao,
    private val settingsRepository: SettingsRepository,
) {
    val tracks: Flow<List<Track>> = trackDao.observeAllTracks().map { entities ->
        entities.map { it.toTrack() }
    }

    val trackCount: Flow<Int> = trackDao.countTracks()

    val librarySources: Flow<List<LibrarySource>> = librarySourceDao.observeAll().map { list ->
        list.map { it.toLibrarySource() }
    }

    val safFolderSources: Flow<List<LibrarySource>> = librarySourceDao.observeSafFolders().map { list ->
        list.map { it.toLibrarySource() }
    }

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
        return try {
            var total = 0
            sources.forEach { source ->
                val scanned = safFolderScanner.scanSource(source)
                trackDao.upsertTracks(scanned.map { it.toEntity() })
                scanned.forEach { track -> trackStatsDao.insertIfAbsent(defaultStatsEntity(track.id)) }
                if (scanned.isNotEmpty()) {
                    trackDao.deleteMissingTracksForSource(scanned.map { it.id }, source.id)
                } else {
                    trackDao.deleteTracksForSource(source.id)
                }
                total += scanned.size
            }
            LibraryScanResult.Success(total, "Selected folders")
        } catch (error: Exception) {
            LibraryScanResult.Error(error.message ?: "Folder scan failed.")
        }
    }

    suspend fun scanFullDeviceLibrary(): LibraryScanResult {
        if (resolvePermissionState() != MediaPermissionState.Granted) {
            return LibraryScanResult.Error("Full device scan requires music access permission.")
        }
        return try {
            settingsRepository.setFullDeviceScanEnabled(true)
            val scanned = mediaStoreScanner.scanFullDevice()
            trackDao.upsertTracks(scanned.map { it.toEntity() })
            scanned.forEach { track -> trackStatsDao.insertIfAbsent(defaultStatsEntity(track.id)) }
            if (scanned.isNotEmpty()) {
                trackDao.deleteMissingFullDeviceTracks(scanned.map { it.id })
            }
            LibraryScanResult.Success(scanned.size, "Full device library")
        } catch (error: Exception) {
            LibraryScanResult.Error(error.message ?: "Full device scan failed.")
        }
    }

    suspend fun getTrackById(id: String): Track? = trackDao.getTrackById(id)?.toTrack()

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
