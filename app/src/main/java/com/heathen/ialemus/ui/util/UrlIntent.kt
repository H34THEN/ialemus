package com.heathen.ialemus.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openUrlInBrowser(context: Context, url: String) {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed))
    context.startActivity(intent)
}
