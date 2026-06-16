package com.heathen.ialemus.core.player

import android.util.Log
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.model.Track

/**
 * Maps a user-selected track to the ExoPlayer queue start index.
 */
object PlaybackIndexMapper {
    private const val TAG = "IalemusPlayback"

    fun resolveStartIndex(tracks: List<Track>, selectedTrackId: String): Int? {
        if (tracks.isEmpty()) return null
        val index = tracks.indexOfFirst { it.id == selectedTrackId }
        return if (index >= 0) index else null
    }

    fun resolveStartIndexInQueue(queue: List<Track>, selectedTrackId: String): Int {
        if (queue.isEmpty()) return 0
        return queue.indexOfFirst { it.id == selectedTrackId }.coerceAtLeast(0)
    }

    fun logSelection(
        selectedTrack: Track,
        listIndex: Int,
        playerStartIndex: Int,
        queueSize: Int,
    ) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG,
            "play trackId=${selectedTrack.id} listIndex=$listIndex playerStartIndex=$playerStartIndex queueSize=$queueSize title=${selectedTrack.title}",
        )
    }
}
