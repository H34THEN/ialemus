package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.RepeatMode
import com.heathen.ialemus.core.model.Track
import kotlin.random.Random

class ShuffleEngine {
    fun orderQueue(
        tracks: List<Track>,
        seed: Long,
        startTrackId: String? = null,
    ): List<Track> {
        if (tracks.isEmpty()) return tracks
        val shuffled = tracks.shuffled(Random(seed))
        if (startTrackId == null) return shuffled
        val start = shuffled.indexOfFirst { it.id == startTrackId }.coerceAtLeast(0)
        return shuffled.drop(start) + shuffled.take(start)
    }

    fun playerRepeatMode(repeatMode: RepeatMode): Int = when (repeatMode) {
        RepeatMode.ONE -> androidx.media3.common.Player.REPEAT_MODE_ONE
        RepeatMode.QUEUE -> androidx.media3.common.Player.REPEAT_MODE_ALL
        RepeatMode.OFF -> androidx.media3.common.Player.REPEAT_MODE_OFF
    }
}
