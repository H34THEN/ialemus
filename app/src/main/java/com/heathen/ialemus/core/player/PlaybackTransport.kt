package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.RepeatMode

/**
 * Pure helpers for safe queue navigation — used by [PlayerConnection] and unit tests.
 */
object PlaybackTransport {
    fun canSkipNext(
        queueSize: Int,
        currentIndex: Int,
        hasNextMediaItem: Boolean,
        repeatMode: RepeatMode,
    ): Boolean {
        if (queueSize <= 0 || currentIndex < 0) return false
        if (hasNextMediaItem) return true
        return repeatMode == RepeatMode.QUEUE && queueSize > 1
    }

    fun canSkipPrevious(
        queueSize: Int,
        currentIndex: Int,
        positionMs: Long,
        hasPreviousMediaItem: Boolean,
        repeatMode: RepeatMode,
    ): Boolean {
        if (queueSize <= 0 || currentIndex < 0) return false
        if (positionMs > RESTART_THRESHOLD_MS) return true
        if (hasPreviousMediaItem) return true
        return repeatMode == RepeatMode.QUEUE && queueSize > 1
    }

    fun resolveNextIndex(queueSize: Int, currentIndex: Int, repeatMode: RepeatMode): Int? {
        if (queueSize <= 0 || currentIndex < 0) return null
        if (currentIndex < queueSize - 1) return currentIndex + 1
        if (repeatMode == RepeatMode.QUEUE && queueSize > 0) return 0
        return null
    }

    fun resolvePreviousIndex(
        queueSize: Int,
        currentIndex: Int,
        positionMs: Long,
        repeatMode: RepeatMode,
    ): Int? {
        if (queueSize <= 0 || currentIndex < 0) return null
        if (positionMs > RESTART_THRESHOLD_MS) return currentIndex
        if (currentIndex > 0) return currentIndex - 1
        if (repeatMode == RepeatMode.QUEUE && queueSize > 0) return queueSize - 1
        return currentIndex
    }

    fun safeQueueIndex(playerIndex: Int, queueSize: Int, fallbackIndex: Int): Int {
        if (queueSize <= 0) return -1
        if (playerIndex in 0 until queueSize) return playerIndex
        if (fallbackIndex in 0 until queueSize) return fallbackIndex
        return -1
    }

    private const val RESTART_THRESHOLD_MS = 3_000L
}
