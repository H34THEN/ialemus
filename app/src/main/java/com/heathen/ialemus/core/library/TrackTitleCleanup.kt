package com.heathen.ialemus.core.library

object TrackTitleCleanup {
    private val PREFIX_PATTERN = Regex("""^(\d{1,3})[\s.\-_]+""")

    fun stripTrackNumberPrefix(title: String): String? {
        val trimmed = title.trim()
        val match = PREFIX_PATTERN.find(trimmed) ?: return null
        val cleaned = trimmed.substring(match.range.last + 1).trim()
        return cleaned.takeIf { it.isNotBlank() && !cleaned.equals(trimmed, ignoreCase = true) }
    }

    fun hasTrackNumberPrefix(title: String): Boolean = stripTrackNumberPrefix(title) != null
}
