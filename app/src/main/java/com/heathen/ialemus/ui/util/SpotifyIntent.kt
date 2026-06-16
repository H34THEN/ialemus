package com.heathen.ialemus.ui.util

import android.content.Context

fun openSpotifyApp(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        openUrlInBrowser(context, "https://open.spotify.com")
    }
}
