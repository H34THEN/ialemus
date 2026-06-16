package com.heathen.ialemus.core.model

enum class NowPlayingLayoutMode(val displayName: String, val description: String) {
    BALANCED(
        displayName = "Balanced",
        description = "Spotify-like standard layout with art, controls, and compact metadata.",
    ),
    IMAGE_HEAVY(
        displayName = "Image Heavy",
        description = "Large cover-forward layout with minimal metadata above the fold.",
    ),
    TEXT_METADATA(
        displayName = "Text + Metadata",
        description = "Text-heavy view with detailed track and source information.",
    ),
    PLAYLIST_RADIO(
        displayName = "Playlist / Radio",
        description = "Queue-focused layout with Up Next preview and transport controls.",
    ),
    CYBERPUNK_HUD(
        displayName = "Cyberpunk HUD",
        description = "Dense EVA command panels, signal chips, and tactical indicators.",
    ),
}
