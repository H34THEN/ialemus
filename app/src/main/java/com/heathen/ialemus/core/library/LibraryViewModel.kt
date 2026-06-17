package com.heathen.ialemus.core.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.AlbumSummary
import com.heathen.ialemus.core.model.ArtistSummary
import com.heathen.ialemus.core.model.FolderSummary
import com.heathen.ialemus.core.model.LibrarySource
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.playlist.M3uImportResult
import com.heathen.ialemus.core.playlist.PlaylistSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val repository = container.libraryRepository
    private val playlistRepository = container.playlistRepository

    val tracks: StateFlow<List<Track>> = repository.tracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    val trackCount: StateFlow<Int> = repository.trackCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0,
    )

    val librarySources: StateFlow<List<LibrarySource>> = repository.librarySources.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val artistSummaries: StateFlow<List<ArtistSummary>> = repository.artistSummaries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val albumSummaries: StateFlow<List<AlbumSummary>> = repository.albumSummaries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val folderSummaries: StateFlow<List<FolderSummary>> = repository.folderSummaries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val audiobookTracks: StateFlow<List<Track>> = repository.audiobookTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val recentlyAddedTracks: StateFlow<List<Track>> = repository.recentlyAddedTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val recentlyPlayedTracks: StateFlow<List<Track>> = repository.recentlyPlayedTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val mostPlayedTracks: StateFlow<List<Track>> = repository.mostPlayedTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val playlists: StateFlow<List<PlaylistSummary>> = playlistRepository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _importResult = MutableStateFlow<M3uImportResult?>(null)
    val importResult: StateFlow<M3uImportResult?> = _importResult.asStateFlow()

    private val _permissionState = MutableStateFlow<MediaPermissionState>(MediaPermissionState.Unknown)
    val permissionState: StateFlow<MediaPermissionState> = _permissionState.asStateFlow()

    private val _scanState = MutableStateFlow<LibraryScanState>(LibraryScanState.NoSources)
    val scanState: StateFlow<LibraryScanState> = _scanState.asStateFlow()

    init {
        refreshPermissionState()
        viewModelScope.launch {
            repository.restorePersistedLibraryAccess()
            refreshScanState()
        }
    }

    fun tracksForArtist(artist: String): StateFlow<List<Track>> =
        repository.tracksForArtist(artist).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun tracksForAlbum(album: String, artist: String): StateFlow<List<Track>> =
        repository.tracksForAlbum(album, artist).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun tracksForFolder(librarySourceId: String, folderPath: String): StateFlow<List<Track>> =
        repository.tracksForFolder(librarySourceId, folderPath).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun refreshPermissionState() {
        _permissionState.value = repository.resolvePermissionState()
    }

    fun refreshScanState() {
        viewModelScope.launch {
            _scanState.value = repository.refreshScanStateHint()
        }
    }

    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        _permissionState.value = when {
            granted -> MediaPermissionState.Granted
            shouldShowRationale -> MediaPermissionState.Denied
            else -> MediaPermissionState.DeniedPermanently
        }
    }

    fun addMusicFolder(uri: Uri, displayName: String) {
        viewModelScope.launch {
            repository.addSafFolder(uri, displayName)
            refreshScanState()
        }
    }

    fun removeMusicFolder(sourceId: String) {
        viewModelScope.launch {
            repository.removeSource(sourceId)
            refreshScanState()
        }
    }

    fun scanSelectedFolders() {
        if (_scanState.value is LibraryScanState.ScanningFolders) return
        viewModelScope.launch {
            _scanState.value = LibraryScanState.ScanningFolders
            when (val result = repository.scanSelectedFolders()) {
                is LibraryScanResult.Success -> applyScanSuccess(result)
                is LibraryScanResult.Error -> {
                    _scanState.value = LibraryScanState.Failed(result.message)
                }
            }
        }
    }

    fun scanPrimaryFolder() {
        if (_scanState.value is LibraryScanState.ScanningFolders) return
        viewModelScope.launch {
            _scanState.value = LibraryScanState.ScanningFolders
            when (val result = repository.scanPrimaryFolder()) {
                is LibraryScanResult.Success -> applyScanSuccess(result)
                is LibraryScanResult.Error -> {
                    _scanState.value = LibraryScanState.Failed(result.message)
                }
            }
        }
    }

    fun scanFullDeviceLibrary() {
        if (_scanState.value is LibraryScanState.ScanningFullDevice) return
        viewModelScope.launch {
            _scanState.value = LibraryScanState.ScanningFullDevice
            when (val result = repository.scanFullDeviceLibrary()) {
                is LibraryScanResult.Success -> {
                    if (result.trackCount > 0) {
                        _scanState.value = LibraryScanState.FullDeviceComplete(result.trackCount)
                    } else if (result.warnings.isNotEmpty()) {
                        _scanState.value = LibraryScanState.Failed(result.warnings.joinToString(" · "))
                    } else {
                        _scanState.value = LibraryScanState.FullDeviceComplete(0)
                    }
                }
                is LibraryScanResult.Error -> {
                    _scanState.value = LibraryScanState.Failed(result.message)
                }
            }
        }
    }

    private fun applyScanSuccess(result: LibraryScanResult.Success) {
        when {
            result.trackCount > 0 -> {
                _scanState.value = LibraryScanState.FolderScanComplete(result.trackCount)
            }
            result.warnings.isNotEmpty() -> {
                _scanState.value = LibraryScanState.Failed(result.warnings.joinToString(" · "))
            }
            else -> {
                _scanState.value = LibraryScanState.FolderScanComplete(0)
            }
        }
    }

    fun requiredPermission(): String = repository.requiredPermission()

    fun createPlaylist(name: String, onCreated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val created = playlistRepository.createPlaylist(name)
            onCreated?.invoke(created.id)
        }
    }

    fun createPlaylistAndAddTrack(name: String, trackId: String) {
        viewModelScope.launch {
            val created = playlistRepository.createPlaylist(name)
            playlistRepository.addTrack(created.id, trackId)
        }
    }

    fun renamePlaylist(playlistId: String, name: String) {
        viewModelScope.launch { playlistRepository.renamePlaylist(playlistId, name) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch { playlistRepository.addTrack(playlistId, trackId) }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch { playlistRepository.removeTrack(playlistId, trackId) }
    }

    suspend fun loadPlaylistTracks(playlistId: String): List<Track> =
        playlistRepository.getTracksForPlaylist(playlistId)

    fun observePlaylistTrackIds(playlistId: String) = playlistRepository.observeTrackIds(playlistId)

    fun importM3uPlaylist(name: String, content: String) {
        viewModelScope.launch {
            val lines = content.lines()
            _importResult.value = playlistRepository.importM3u(name, lines)
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                return LibraryViewModel(container) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
