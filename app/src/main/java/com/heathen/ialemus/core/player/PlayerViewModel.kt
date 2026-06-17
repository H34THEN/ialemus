package com.heathen.ialemus.core.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.QueueItem
import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import android.net.Uri
import com.heathen.ialemus.core.lyrics.LyricsRepository
import com.heathen.ialemus.core.player.PlaybackAudioSessionHolder
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.core.model.NowPlayingLayoutMode
import com.heathen.ialemus.core.visualizer.AudioVisualizerState
import com.heathen.ialemus.data.local.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.heathen.ialemus.core.library.withOverrides

class PlayerViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val playerConnection = container.playerConnection
    private val queueRepository = container.queueRepository
    private val trackStatsDao = container.trackStatsDao
    private val trackOverrideRepository = container.trackOverrideRepository
    private val lyricsRepository = container.lyricsRepository
    private val settingsRepository = container.settingsRepository
    private val audioVisualizerController = container.audioVisualizerController

    init {
        viewModelScope.launch {
            combine(
                playerConnection.playbackState,
                trackOverrideRepository.overrides,
                settingsRepository.reactiveVisualizerEnabled,
                settingsRepository.dapModeEnabled,
                settingsRepository.nowPlayingLayoutMode,
            ) { state, overrides, reactiveEnabled, dapMode, layoutMode ->
                Triple(
                    state.withTrackOverrides(overrides),
                    reactiveEnabled && !dapMode,
                    layoutMode == NowPlayingLayoutMode.CYBERPUNK_HUD,
                )
            }.collect { (merged, reactiveEnabled, cyberpunkLayout) ->
                if (!cyberpunkLayout) {
                    audioVisualizerController.release()
                    return@collect
                }
                audioVisualizerController.setReactiveEnabled(
                    enabled = reactiveEnabled,
                    permissionGranted = container.hasRecordAudioPermission(),
                )
                audioVisualizerController.onPlaybackChanged(merged, merged.currentTrack)
            }
        }
        viewModelScope.launch {
            PlaybackAudioSessionHolder.audioSessionId.collect { sessionId ->
                audioVisualizerController.onAudioSessionChanged(sessionId)
            }
        }
    }

    override fun onCleared() {
        audioVisualizerController.release()
        super.onCleared()
    }

    val visualizerState: StateFlow<AudioVisualizerState> = audioVisualizerController.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioVisualizerState(),
    )

    val playbackState: StateFlow<PlaybackState> = combine(
        playerConnection.playbackState,
        trackOverrideRepository.overrides,
    ) { state, overrides ->
        state.withTrackOverrides(overrides)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlaybackState(),
    )

    val queueItems: StateFlow<List<QueueItem>> = combine(
        queueRepository.activeQueue,
        queueRepository.currentIndex,
        trackOverrideRepository.overrides,
    ) { queue, index, overrides ->
        queue.mapIndexed { itemIndex, track ->
            QueueItem(
                track = track.withOverrides(overrides[track.id]),
                queueIndex = itemIndex,
                isCurrent = itemIndex == index,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val shuffleEnabled: StateFlow<Boolean> = queueRepository.shuffleEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val repeatMode: StateFlow<RepeatMode> = queueRepository.repeatMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RepeatMode.OFF,
    )

    val shuffleMode: StateFlow<ShuffleMode> = combine(
        queueRepository.shuffleEnabled,
        queueRepository.repeatMode,
    ) { _, _ ->
        queueRepository.legacyShuffleMode()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShuffleMode.DEFAULT,
    )

    fun connect() {
        viewModelScope.launch {
            playerConnection.connect()
        }
    }

    fun disconnect() {
        playerConnection.disconnect()
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        playerConnection.playTracks(tracks, startIndex)
    }

    fun playTrack(tracks: List<Track>, track: Track) {
        playerConnection.playTrackById(tracks, track.id)
    }

    fun playCollection(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        if (shuffle != queueRepository.shuffleEnabled.value) {
            queueRepository.toggleShuffle()
        }
        playerConnection.playTracks(tracks, 0)
    }

    fun playPause() = playerConnection.playPause()

    fun seekTo(positionMs: Long) = playerConnection.seekTo(positionMs)

    fun skipToPrevious() = playerConnection.skipToPrevious()

    fun skipToNext() = playerConnection.skipToNext()

    fun playQueueItem(index: Int) = playerConnection.playQueueItem(index)

    fun toggleShuffle() = playerConnection.toggleShuffle()

    fun cycleRepeat() = playerConnection.cycleRepeat()

    fun setShuffleMode(mode: ShuffleMode) = playerConnection.setShuffleMode(mode)

    fun clearPlaybackError() = playerConnection.clearPlaybackError()

    fun toggleFavorite(trackId: String, favorite: Boolean) {
        viewModelScope.launch {
            trackStatsDao.setFavorite(trackId, favorite)
        }
    }

    fun observeFavorite(trackId: String): Flow<Boolean> =
        trackStatsDao.observeStatsForTrack(trackId).map { stats -> stats?.favorite == true }

    fun saveDisplayTitleOverride(trackId: String, displayTitle: String) {
        viewModelScope.launch {
            trackOverrideRepository.saveTitleOverride(trackId, displayTitle)
        }
    }

    fun clearDisplayTitleOverride(trackId: String) {
        viewModelScope.launch {
            trackOverrideRepository.clearOverride(trackId)
        }
    }

    fun saveDisplayOverrides(
        trackId: String,
        title: String?,
        artist: String?,
        album: String?,
    ) {
        viewModelScope.launch {
            trackOverrideRepository.saveAllOverrides(trackId, title, artist, album)
        }
    }

    fun clearAllOverrides(trackId: String) = clearDisplayTitleOverride(trackId)

    fun observeTrackStats(trackId: String) = trackStatsDao.observeStatsForTrack(trackId)

    fun setPlaybackSpeed(speed: Float) = playerConnection.setPlaybackSpeed(speed)

    fun setSleepTimer(minutes: Int?) = playerConnection.setSleepTimer(minutes)

    fun observeTrackOverride(trackId: String) = trackOverrideRepository.observeForTrack(trackId)

    fun observeLyrics(trackId: String): Flow<LyricsEntity?> = lyricsRepository.observeLyrics(trackId)

    fun saveManualLyrics(trackId: String, rawText: String) {
        viewModelScope.launch { lyricsRepository.saveManualLyrics(trackId, rawText) }
    }

    fun clearLyrics(trackId: String) {
        viewModelScope.launch { lyricsRepository.clearLyrics(trackId) }
    }

    fun importLyricsFile(trackId: String, uri: Uri) {
        viewModelScope.launch { lyricsRepository.importFromUri(trackId, uri) }
    }

    fun scanSidecarLyrics(track: Track, treeUri: String?) {
        viewModelScope.launch { lyricsRepository.scanSidecarForTrack(track, treeUri) }
    }

    fun tryEmbeddedLyrics(track: Track) {
        viewModelScope.launch { lyricsRepository.tryExtractEmbedded(track) }
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(container) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
