package com.heathen.ialemus.core.model

data class FolderSummary(
    val librarySourceId: String,
    val sourceLabel: String,
    val folderPath: String,
    val trackCount: Int,
) {
    val displayName: String
        get() = when {
            folderPath.isBlank() || folderPath == "/" -> sourceLabel
            else -> folderPath.substringAfterLast('/').ifBlank { sourceLabel }
        }

    val safePathLabel: String
        get() = when {
            folderPath.isBlank() || folderPath == "/" -> sourceLabel
            else -> folderPath.trimEnd('/').take(64)
        }
}

enum class LibraryBrowseMode(val label: String) {
    TRACKS("Tracks"),
    ARTISTS("Artists"),
    ALBUMS("Albums"),
    GENRES("Genres"),
    PLAYLISTS("Playlists"),
    FOLDERS("Folders"),
    AUDIOBOOKS("Audiobooks"),
    SIGNAL("Signal"),
}

enum class SignalBrowseMode(val label: String) {
    FAVORITES("Favorites"),
    RECENTLY_ADDED("Recently Added"),
    RECENTLY_PLAYED("Recently Played"),
    MOST_PLAYED("Most Played"),
}

sealed class LibraryDetail {
    data class Artist(val artist: String) : LibraryDetail()
    data class Album(val album: String, val artist: String) : LibraryDetail()
    data class Folder(
        val librarySourceId: String,
        val folderPath: String,
        val displayName: String,
    ) : LibraryDetail()
}
