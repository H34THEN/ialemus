package com.heathen.ialemus.core.lyrics

data class LyricLine(
    val timestampMs: Long,
    val text: String,
)

data class ParsedLyrics(
    val lines: List<LyricLine>,
    val isSynced: Boolean,
    val plainText: String,
)

object LyricsParser {
    private val LRC_TIMESTAMP = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?\]""")

    fun parse(rawText: String): ParsedLyrics {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return ParsedLyrics(emptyList(), isSynced = false, plainText = "")
        }
        val lines = mutableListOf<LyricLine>()
        trimmed.lineSequence().forEach { line ->
            val text = line.trim()
            if (text.isEmpty()) return@forEach
            val timestamps = LRC_TIMESTAMP.findAll(text).toList()
            if (timestamps.isEmpty()) {
                lines += LyricLine(timestampMs = -1L, text = text)
                return@forEach
            }
            val lyricText = LRC_TIMESTAMP.replace(text, "").trim()
            if (lyricText.isEmpty()) return@forEach
            timestamps.forEach { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                val fraction = match.groupValues.getOrNull(3).orEmpty()
                val fractionMs = when (fraction.length) {
                    0 -> 0L
                    1 -> (fraction.toLongOrNull() ?: 0L) * 100L
                    2 -> (fraction.toLongOrNull() ?: 0L) * 10L
                    else -> (fraction.toLongOrNull() ?: 0L)
                }
                val timestampMs = minutes * 60_000L + seconds * 1_000L + fractionMs
                lines += LyricLine(timestampMs = timestampMs, text = lyricText)
            }
        }
        val synced = lines.any { it.timestampMs >= 0 }
        val sorted = if (synced) {
            lines.filter { it.timestampMs >= 0 }.sortedBy { it.timestampMs }
        } else {
            lines
        }
        val plain = if (synced) {
            sorted.joinToString("\n") { it.text }
        } else {
            trimmed
        }
        return ParsedLyrics(lines = sorted, isSynced = synced, plainText = plain)
    }

    fun currentLineIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty() || positionMs < 0) return -1
        var index = -1
        lines.forEachIndexed { i, line ->
            if (line.timestampMs >= 0 && line.timestampMs <= positionMs) {
                index = i
            }
        }
        return index
    }
}
