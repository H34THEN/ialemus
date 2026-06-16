package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.ShuffleMode
import com.heathen.ialemus.core.model.Track
import kotlin.random.Random

class ShuffleEngine {
    fun orderQueue(
        tracks: List<Track>,
        mode: ShuffleMode,
        seed: Long,
        startTrackId: String? = null,
    ): List<Track> {
        if (tracks.isEmpty()) return tracks
        return when (mode) {
            ShuffleMode.TRUE_RANDOM -> {
                val shuffled = tracks.shuffled(Random(seed))
                if (startTrackId == null) {
                    shuffled
                } else {
                    val start = shuffled.indexOfFirst { it.id == startTrackId }.coerceAtLeast(0)
                    shuffled.drop(start) + shuffled.take(start)
                }
            }
            else -> tracks
        }
    }

    fun playerRepeatMode(mode: ShuffleMode): Int = when (mode) {
        ShuffleMode.REPEAT_ONE -> androidx.media3.common.Player.REPEAT_MODE_ONE
        ShuffleMode.REPEAT_QUEUE -> androidx.media3.common.Player.REPEAT_MODE_ALL
        else -> androidx.media3.common.Player.REPEAT_MODE_OFF
    }
}
