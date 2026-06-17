package com.heathen.ialemus.core.lyrics

import com.heathen.ialemus.data.local.entity.LyricsSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsParserTest {
    @Test
    fun parseSyncedLrc() {
        val raw = """
            [00:12.50]First line
            [01:02]Second line
        """.trimIndent()
        val parsed = LyricsParser.parse(raw)
        assertTrue(parsed.isSynced)
        assertEquals(2, parsed.lines.size)
        assertEquals(12_500L, parsed.lines[0].timestampMs)
        assertEquals("First line", parsed.lines[0].text)
    }

    @Test
    fun parsePlainText() {
        val raw = "Line one\nLine two"
        val parsed = LyricsParser.parse(raw)
        assertEquals(false, parsed.isSynced)
        assertEquals(raw, parsed.plainText)
    }

    @Test
    fun currentLineIndexAdvances() {
        val lines = listOf(
            com.heathen.ialemus.core.lyrics.LyricLine(10_000L, "A"),
            com.heathen.ialemus.core.lyrics.LyricLine(20_000L, "B"),
        )
        assertEquals(0, LyricsParser.currentLineIndex(lines, 15_000L))
        assertEquals(1, LyricsParser.currentLineIndex(lines, 25_000L))
    }
}
