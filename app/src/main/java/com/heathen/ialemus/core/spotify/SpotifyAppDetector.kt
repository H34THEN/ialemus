package com.heathen.ialemus.core.spotify

import android.content.Context
import android.content.pm.PackageManager

object SpotifyAppDetector {
    fun detect(context: Context): SpotifyAppStatus {
        return try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(SpotifyDefaults.SPOTIFY_PACKAGE, 0)
            SpotifyAppStatus.INSTALLED
        } catch (_: PackageManager.NameNotFoundException) {
            SpotifyAppStatus.NOT_INSTALLED
        } catch (_: Exception) {
            SpotifyAppStatus.UNKNOWN
        }
    }

    fun isInstalled(context: Context): Boolean =
        detect(context) == SpotifyAppStatus.INSTALLED
}
