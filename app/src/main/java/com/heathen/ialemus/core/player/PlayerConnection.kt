package com.heathen.ialemus.core.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.widget.IalemusPlaybackWidgetProvider
import com.heathen.ialemus.widget.WidgetStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.heathen.ialemus.core.diagnostics.StabilityDiagnostics

private const val TAG = "PlayerConnection"
private const val PLAYBACK_ERROR_MESSAGE = "Playback link failed. Try rescanning this source."
private const val PLAYBACK_FAILED_MESSAGE = "Playback failed. Try rescanning this source."
private const val TRACK_UNAVAILABLE_MESSAGE = "Track unavailable. Rescan or reselect the source."

class PlayerConnection(
    private val context: Context,
    private val queueRepository: QueueRepository,
    private val shuffleEngine: ShuffleEngine,
    private val widgetStateStore: WidgetStateStore? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tickerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var controller: MediaController? = null
    private var lastWidgetTitle: String? = null
    private var lastWidgetPlaying: Boolean? = null
    private var sleepTimerJob: kotlinx.coroutines.Job? = null
    private var positionTickerJob: Job? = null
    private var lastPublishedPositionMs = -1L

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateFromPlayer()
            maybeStopForEndOfTrackSleep()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateFromPlayer()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: C.INDEX_UNSET
            if (index != C.INDEX_UNSET) {
                queueRepository.updateCurrentIndex(index)
            }
            updateFromPlayer()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            updateFromPlayer()
        }

        override fun onPlayerError(error: PlaybackException) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Player error code=${error.errorCodeName} message=${error.message}", error)
            }
            setPlaybackError(PLAYBACK_FAILED_MESSAGE)
        }
    }

    suspend fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, IalemusPlaybackService::class.java),
        )
        controller = MediaController.Builder(context, sessionToken).buildAsync().await()
        controller?.addListener(playerListener)
        _playbackState.update { it.copy(isConnected = true) }
        updateFromPlayer()
        startPositionUpdates()
    }

    fun disconnect() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
        scope.cancel()
        _playbackState.update { it.copy(isConnected = false) }
    }

    fun clearPlaybackError() {
        _playbackState.update { it.copy(playbackError = null) }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) {
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
            return
        }
        val safeStart = startIndex.coerceIn(0, tracks.lastIndex)
        val selectedTrack = tracks[safeStart]
        val playerStartIndex = queueRepository.setQueue(tracks, safeStart)
        val queue = queueRepository.activeQueue.value
        val mediaController = controller
        if (mediaController == null) {
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
            return
        }

        try {
            PlaybackIndexMapper.logSelection(
                selectedTrack = selectedTrack,
                listIndex = safeStart,
                playerStartIndex = playerStartIndex,
                queueSize = queue.size,
            )
            logTransportState("playTracks")

            mediaController.shuffleModeEnabled = false
            mediaController.repeatMode = queueRepository.playerRepeatMode()
            mediaController.setMediaItems(queue.toMediaItems(), playerStartIndex, 0L)
            mediaController.prepare()
            mediaController.play()
            clearPlaybackError()
            updateFromPlayer()
        } catch (error: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "playTracks failed for trackId=${selectedTrack.id}", error)
            }
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
        }
    }

    fun playTrackById(tracks: List<Track>, trackId: String) {
        val startIndex = PlaybackIndexMapper.resolveStartIndex(tracks, trackId)
        if (startIndex == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "playTrackById could not resolve trackId=$trackId")
            }
            setPlaybackError(PLAYBACK_ERROR_MESSAGE)
            return
        }
        playTracks(tracks, startIndex)
    }

    fun playPause() {
        val mediaController = controller ?: return
        try {
            if (mediaController.isPlaying) {
                mediaController.pause()
            } else {
                mediaController.play()
            }
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("playPause", error)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            controller?.seekTo(positionMs.coerceAtLeast(0L))
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("seekTo", error)
        }
    }

    fun skipToPrevious() {
        val mediaController = controller ?: return
        val queue = queueRepository.activeQueue.value
        if (queue.isEmpty()) return

        try {
            val currentIndex = PlaybackTransport.safeQueueIndex(
                playerIndex = mediaController.currentMediaItemIndex,
                queueSize = queue.size,
                fallbackIndex = queueRepository.currentIndex.value,
            )
            if (currentIndex < 0) return

            logTransportState("skipToPrevious")
            val targetIndex = PlaybackTransport.resolvePreviousIndex(
                queueSize = queue.size,
                currentIndex = currentIndex,
                positionMs = mediaController.currentPosition,
                repeatMode = queueRepository.repeatMode.value,
            )

            when {
                mediaController.currentPosition > 3_000L -> mediaController.seekTo(0)
                targetIndex != null && targetIndex != currentIndex -> seekToQueueIndex(mediaController, targetIndex)
                mediaController.hasPreviousMediaItem() -> mediaController.seekToPreviousMediaItem()
                else -> mediaController.seekTo(0)
            }
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("skipToPrevious", error)
        }
    }

    fun skipToNext() {
        val mediaController = controller ?: return
        val queue = queueRepository.activeQueue.value
        if (queue.isEmpty()) return

        try {
            val currentIndex = PlaybackTransport.safeQueueIndex(
                playerIndex = mediaController.currentMediaItemIndex,
                queueSize = queue.size,
                fallbackIndex = queueRepository.currentIndex.value,
            )
            if (currentIndex < 0) return

            logTransportState("skipToNext")
            val canNext = PlaybackTransport.canSkipNext(
                queueSize = queue.size,
                currentIndex = currentIndex,
                hasNextMediaItem = mediaController.hasNextMediaItem(),
                repeatMode = queueRepository.repeatMode.value,
            )
            if (!canNext) {
                if (BuildConfig.DEBUG) Log.d(TAG, "skipToNext: no-op at end")
                return
            }

            when {
                mediaController.hasNextMediaItem() -> mediaController.seekToNextMediaItem()
                else -> {
                    val nextIndex = PlaybackTransport.resolveNextIndex(
                        queueSize = queue.size,
                        currentIndex = currentIndex,
                        repeatMode = queueRepository.repeatMode.value,
                    ) ?: return
                    seekToQueueIndex(mediaController, nextIndex)
                }
            }
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("skipToNext", error)
        }
    }

    fun playQueueItem(index: Int) {
        val queue = queueRepository.activeQueue.value
        val track = queue.getOrNull(index) ?: return
        val mediaController = controller ?: return
        try {
            seekToQueueIndex(mediaController, index)
            mediaController.play()
            queueRepository.updateCurrentIndex(index)
            PlaybackIndexMapper.logSelection(
                selectedTrack = track,
                listIndex = index,
                playerStartIndex = index,
                queueSize = queue.size,
            )
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("playQueueItem", error)
        }
    }

    fun toggleShuffle(): Boolean = queueRepository.toggleShuffle().also {
        syncPlayerQueue()
    }

    fun cycleRepeat(): RepeatMode {
        val mode = queueRepository.cycleRepeat()
        try {
            controller?.repeatMode = queueRepository.playerRepeatMode()
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("cycleRepeat", error)
        }
        return mode
    }

    fun setShuffleMode(mode: ShuffleMode) {
        queueRepository.setShuffleMode(mode)
        syncPlayerQueue()
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2f)
        try {
            controller?.setPlaybackSpeed(clamped)
            _playbackState.update { it.copy(playbackSpeed = clamped) }
        } catch (error: Exception) {
            handleTransportError("setPlaybackSpeed", error)
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        if (minutes == null || minutes == 0) {
            _playbackState.update { it.copy(sleepTimerMinutes = null, sleepTimerEndsAtMs = null) }
            return
        }
        if (minutes < 0) {
            _playbackState.update { it.copy(sleepTimerMinutes = minutes, sleepTimerEndsAtMs = null) }
            return
        }
        val endsAt = System.currentTimeMillis() + minutes * 60_000L
        _playbackState.update { it.copy(sleepTimerMinutes = minutes, sleepTimerEndsAtMs = endsAt) }
        sleepTimerJob = scope.launch {
            delay(minutes * 60_000L)
            try {
                controller?.pause()
                updateFromPlayer()
            } catch (_: Exception) {
            }
            _playbackState.update { it.copy(sleepTimerMinutes = null, sleepTimerEndsAtMs = null) }
            sleepTimerJob = null
        }
    }

    private fun maybeStopForEndOfTrackSleep() {
        val state = _playbackState.value
        if (state.sleepTimerMinutes != -1) return
        val mc = controller ?: return
        if (!mc.isPlaying && mc.playbackState == Player.STATE_ENDED) {
            mc.pause()
            _playbackState.update { it.copy(sleepTimerMinutes = null, sleepTimerEndsAtMs = null) }
        }
    }

    private fun syncPlayerQueue() {
        val mediaController = controller ?: return
        val queue = queueRepository.activeQueue.value
        if (queue.isEmpty()) return
        val index = queueRepository.currentIndex.value.coerceIn(queue.indices)
        try {
            val position = mediaController.currentPosition
            mediaController.shuffleModeEnabled = false
            mediaController.repeatMode = queueRepository.playerRepeatMode()
            mediaController.setMediaItems(queue.toMediaItems(), index, position)
            if (mediaController.isPlaying) {
                mediaController.play()
            }
            updateFromPlayer()
        } catch (error: Exception) {
            handleTransportError("syncPlayerQueue", error)
        }
    }

    private fun seekToQueueIndex(mediaController: MediaController, index: Int) {
        val queue = queueRepository.activeQueue.value
        if (index !in queue.indices) return
        mediaController.seekToDefaultPosition(index)
    }

    private fun startPositionUpdates() {
        positionTickerJob?.cancel()
        positionTickerJob = tickerScope.launch {
            while (controller != null) {
                val playing = controller?.isPlaying == true
                val intervalMs = if (playing) 400L else 1_000L
                withContext(Dispatchers.Main.immediate) {
                    updateFromPlayerThrottled()
                }
                StabilityDiagnostics.playbackTicker(intervalMs, playing)
                delay(intervalMs)
            }
        }
    }

    private fun updateFromPlayerThrottled() {
        val mediaController = controller ?: return
        val positionMs = mediaController.currentPosition
        val playing = mediaController.isPlaying
        if (!playing &&
            positionMs == lastPublishedPositionMs &&
            _playbackState.value.isPlaying == false &&
            _playbackState.value.positionMs == positionMs
        ) {
            return
        }
        lastPublishedPositionMs = positionMs
        updateFromPlayer()
    }

    private fun updateFromPlayer() {
        val mediaController = controller
        val queue = queueRepository.activeQueue.value
        val rawPlayerIndex = mediaController?.currentMediaItemIndex ?: C.INDEX_UNSET
        val fallbackIndex = queueRepository.currentIndex.value
        val safeIndex = PlaybackTransport.safeQueueIndex(
            playerIndex = rawPlayerIndex,
            queueSize = queue.size,
            fallbackIndex = fallbackIndex,
        )
        if (safeIndex >= 0) {
            queueRepository.updateCurrentIndex(safeIndex)
        }
        val currentTrack = queue.getOrNull(safeIndex)
            ?: mediaController?.currentMediaItem?.mediaId?.let { mediaId ->
                queue.find { it.id == mediaId }
            }
        val positionMs = mediaController?.currentPosition ?: 0L
        val repeatMode = queueRepository.repeatMode.value
        val shuffleEnabled = queueRepository.shuffleEnabled.value
        val canSkipNext = mediaController?.let { mc ->
            PlaybackTransport.canSkipNext(
                queueSize = queue.size,
                currentIndex = safeIndex,
                hasNextMediaItem = mc.hasNextMediaItem(),
                repeatMode = repeatMode,
            )
        } ?: false
        val canSkipPrevious = mediaController?.let { mc ->
            PlaybackTransport.canSkipPrevious(
                queueSize = queue.size,
                currentIndex = safeIndex,
                positionMs = positionMs,
                hasPreviousMediaItem = mc.hasPreviousMediaItem(),
                repeatMode = repeatMode,
            )
        } ?: false

        _playbackState.update { previous ->
            previous.copy(
                currentTrack = currentTrack,
                isPlaying = mediaController?.isPlaying == true,
                isBuffering = mediaController?.playbackState == Player.STATE_BUFFERING,
                positionMs = positionMs,
                durationMs = mediaController?.duration?.takeIf { value -> value > 0 }
                    ?: currentTrack?.durationMs
                    ?: 0L,
                queueIndex = safeIndex,
                queueSize = queue.size,
                isConnected = mediaController != null,
                canSkipNext = canSkipNext && currentTrack != null,
                canSkipPrevious = canSkipPrevious && currentTrack != null,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
            )
        }
        syncWidget(
            track = currentTrack,
            isPlaying = mediaController?.isPlaying == true,
        )
    }

    private fun syncWidget(track: Track?, isPlaying: Boolean) {
        val store = widgetStateStore ?: return
        val title = track?.title
        if (title == lastWidgetTitle && isPlaying == lastWidgetPlaying) return
        lastWidgetTitle = title
        lastWidgetPlaying = isPlaying
        store.update(title, track?.displayArtist, isPlaying)
        IalemusPlaybackWidgetProvider.refreshAll(context)
    }

    private fun handleTransportError(action: String, error: Exception) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "$action failed", error)
        }
        val message = if (error is PlaybackException || error.message?.contains("uri", ignoreCase = true) == true) {
            TRACK_UNAVAILABLE_MESSAGE
        } else {
            PLAYBACK_FAILED_MESSAGE
        }
        setPlaybackError(message)
    }

    private fun logTransportState(action: String) {
        if (!BuildConfig.DEBUG) return
        val mc = controller
        Log.d(
            TAG,
            "$action queueSize=${queueRepository.activeQueue.value.size} " +
                "index=${mc?.currentMediaItemIndex} " +
                "repeat=${queueRepository.repeatMode.value} " +
                "shuffle=${queueRepository.shuffleEnabled.value} " +
                "mediaId=${mc?.currentMediaItem?.mediaId}",
        )
    }

    private fun setPlaybackError(message: String) {
        _playbackState.update { it.copy(playbackError = message) }
    }
}
