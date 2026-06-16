package com.heathen.ialemus.core.player

import com.heathen.ialemus.core.model.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTransportTest {
    @Test
    fun resolveNextIndex_atEndWithRepeatOff_returnsNull() {
        assertNull(PlaybackTransport.resolveNextIndex(queueSize = 3, currentIndex = 2, repeatMode = RepeatMode.OFF))
    }

    @Test
    fun resolveNextIndex_atEndWithRepeatQueue_wrapsToZero() {
        assertEquals(0, PlaybackTransport.resolveNextIndex(queueSize = 3, currentIndex = 2, repeatMode = RepeatMode.QUEUE))
    }

    @Test
    fun resolvePreviousIndex_atStart_restartsCurrent() {
        assertEquals(0, PlaybackTransport.resolvePreviousIndex(queueSize = 3, currentIndex = 0, positionMs = 0L, repeatMode = RepeatMode.OFF))
    }

    @Test
    fun resolvePreviousIndex_afterThreshold_restartsCurrent() {
        assertEquals(1, PlaybackTransport.resolvePreviousIndex(queueSize = 3, currentIndex = 1, positionMs = 5000L, repeatMode = RepeatMode.OFF))
    }

    @Test
    fun canSkipNext_emptyQueue_isFalse() {
        assertFalse(PlaybackTransport.canSkipNext(queueSize = 0, currentIndex = -1, hasNextMediaItem = false, repeatMode = RepeatMode.OFF))
    }

    @Test
    fun canSkipNext_atEndRepeatQueue_isTrue() {
        assertTrue(PlaybackTransport.canSkipNext(queueSize = 3, currentIndex = 2, hasNextMediaItem = false, repeatMode = RepeatMode.QUEUE))
    }

    @Test
    fun safeQueueIndex_clampsInvalidPlayerIndex() {
        assertEquals(1, PlaybackTransport.safeQueueIndex(playerIndex = 99, queueSize = 3, fallbackIndex = 1))
        assertEquals(-1, PlaybackTransport.safeQueueIndex(playerIndex = -1, queueSize = 0, fallbackIndex = 0))
    }
}
