package com.heathen.ialemus.core.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heathen.ialemus.AppContainer
import com.heathen.ialemus.core.model.QueueItem
import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.library.withOverrides
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val playerConnection = container.playerConnection
    private val queueRepository = container.queueRepository
    private val trackStatsDao = container.trackStatsDao
    private val trackOverrideRepository = container.trackOverrideRepository

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

    fun observeTrackOverride(trackId: String) = trackOverrideRepository.observeForTrack(trackId)

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
