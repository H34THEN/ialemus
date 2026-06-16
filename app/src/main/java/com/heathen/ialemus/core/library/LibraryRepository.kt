package com.heathen.ialemus.core.library

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.data.local.dao.TrackDao
import com.heathen.ialemus.data.local.dao.TrackStatsDao
import com.heathen.ialemus.data.local.defaultStatsEntity
import com.heathen.ialemus.data.local.toEntity
import com.heathen.ialemus.data.local.toTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepository(
    private val context: Context,
    private val scanner: MediaStoreScanner,
    private val trackDao: TrackDao,
    private val trackStatsDao: TrackStatsDao,
) {
    val tracks: Flow<List<Track>> = trackDao.observeAllTracks().map { entities ->
        entities.map { it.toTrack() }
    }

    val trackCount: Flow<Int> = trackDao.countTracks()

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

    suspend fun scanLocalLibrary(): LibraryScanResult {
        if (resolvePermissionState() != MediaPermissionState.Granted) {
            return LibraryScanResult.Error("Music access permission not granted.")
        }

        return try {
            val scanned = scanner.scan()
            trackDao.upsertTracks(scanned.map { it.toEntity() })
            scanned.forEach { track ->
                trackStatsDao.insertIfAbsent(defaultStatsEntity(track.id))
            }
            if (scanned.isNotEmpty()) {
                trackDao.deleteMissingTracks(scanned.map { it.id })
            }
            LibraryScanResult.Success(scanned.size)
        } catch (error: Exception) {
            LibraryScanResult.Error(error.message ?: "Library scan failed.")
        }
    }

    suspend fun getTrackById(id: String): Track? = trackDao.getTrackById(id)?.toTrack()
}
