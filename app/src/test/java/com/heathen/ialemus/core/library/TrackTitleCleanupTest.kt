package com.heathen.ialemus.core.library

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackTitleCleanupTest {
    @Test
    fun stripTrackNumberPrefix_handlesCommonPatterns() {
        assertEquals("Song Title", TrackTitleCleanup.stripTrackNumberPrefix("01 - Song Title"))
        assertEquals("Song Title", TrackTitleCleanup.stripTrackNumberPrefix("01. Song Title"))
        assertEquals("Song Title", TrackTitleCleanup.stripTrackNumberPrefix("1 - Song Title"))
        assertEquals("Song Title", TrackTitleCleanup.stripTrackNumberPrefix("001 - Song Title"))
    }

    @Test
    fun stripTrackNumberPrefix_returnsNullWhenNoPrefix() {
        assertEquals(null, TrackTitleCleanup.stripTrackNumberPrefix("Song Title"))
    }
}
