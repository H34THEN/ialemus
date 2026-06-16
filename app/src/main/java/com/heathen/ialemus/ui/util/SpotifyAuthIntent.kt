package com.heathen.ialemus.ui.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.heathen.ialemus.core.spotify.SpotifyDefaults

fun openSpotifyAuth(context: Context, url: String) {
    runCatching {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }.onFailure {
        openUrlInBrowser(context, url)
    }
}

fun isSpotifyAuthCallback(uri: Uri?): Boolean =
    uri?.scheme == SpotifyDefaults.REDIRECT_SCHEME &&
        uri.host == SpotifyDefaults.REDIRECT_HOST
