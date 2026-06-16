package com.heathen.ialemus.ui.util

import android.content.Context
import com.heathen.ialemus.core.spotify.SpotifyDefaults

fun openSpotifyApp(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(SpotifyDefaults.SPOTIFY_PACKAGE)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        openUrlInBrowser(context, "https://open.spotify.com")
    }
}

fun openSpotifyWeb(context: Context) {
    openUrlInBrowser(context, "https://open.spotify.com")
}

fun openSpotifyUri(context: Context, uri: String) {
    val trimmed = uri.trim()
    if (trimmed.isBlank()) return
    val spotifyIntent = context.packageManager.getLaunchIntentForPackage(SpotifyDefaults.SPOTIFY_PACKAGE)
    if (spotifyIntent != null && trimmed.startsWith("spotify:")) {
        try {
            context.startActivity(
                android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(trimmed))
                    .setPackage(SpotifyDefaults.SPOTIFY_PACKAGE),
            )
            return
        } catch (_: Exception) {
            // fall through to browser
        }
    }
    val webUrl = when {
        trimmed.startsWith("http") -> trimmed
        trimmed.startsWith("spotify:") -> "https://open.spotify.com/"
        else -> trimmed
    }
    openUrlInBrowser(context, webUrl)
}
