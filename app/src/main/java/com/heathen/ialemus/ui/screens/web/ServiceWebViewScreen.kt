package com.heathen.ialemus.ui.screens.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser

@Composable
fun ServiceWebViewScreen(
    state: ServiceWebViewState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var currentUrl by remember(state.url) { mutableStateOf(state.url) }

    BackHandler {
        val view = webView
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            onClose()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.surfaceDeep.copy(alpha = 0.98f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPad, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = {
                val view = webView
                if (view != null && view.canGoBack()) view.goBack() else onClose()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = tokens.accentActive,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.serviceName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.accentActive,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = currentUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { webView?.reload() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = tokens.glowColor,
                )
            }
            IconButton(onClick = { openUrlInBrowser(context, currentUrl) }) {
                Icon(
                    imageVector = Icons.Filled.OpenInBrowser,
                    contentDescription = "Open external browser",
                    tint = tokens.glowColor,
                )
            }
        }

        if (isLoading && loadProgress < 100) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = tokens.accentActive,
                trackColor = tokens.hudBorderColor.copy(alpha = 0.3f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPad)
                .padding(bottom = 4.dp)
                .border(1.dp, tokens.hudBorderColor.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .background(tokens.panelOverlay, MaterialTheme.shapes.medium),
        ) {
            HudServiceWebView(
                url = state.url,
                onWebViewReady = { webView = it },
                onLoadingChanged = { isLoading = it },
                onProgressChanged = { loadProgress = it },
                onUrlChanged = { currentUrl = it },
                onError = { pageError = it },
                modifier = Modifier.fillMaxSize(),
            )

            if (isLoading && loadProgress == 0 && pageError == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = tokens.accentActive,
                )
            }

            if (pageError != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HudStatusChip(label = "LOAD FAILED", warning = true)
                    Text(
                        text = pageError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                    HudButton(
                        label = "Retry",
                        onClick = {
                            pageError = null
                            webView?.reload()
                        },
                        accent = HudButtonAccent.Primary,
                    )
                    HudButton(
                        label = "Open external browser",
                        onClick = { openUrlInBrowser(context, state.url) },
                        accent = HudButtonAccent.Neutral,
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HudServiceWebView(
    url: String,
    onWebViewReady: (WebView) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onUrlChanged: (String) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLoadingChanged(true)
                        onError(null)
                        url?.let(onUrlChanged)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadingChanged(false)
                        url?.let(onUrlChanged)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onLoadingChanged(false)
                            onError(error?.description?.toString() ?: "Could not load page.")
                        }
                    }
                }
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                        if (newProgress >= 100) onLoadingChanged(false)
                    }
                }
                onWebViewReady(this)
                loadUrl(url)
            }
        },
        update = { view ->
            if (view.url != url && view.originalUrl != url) {
                view.loadUrl(url)
            }
        },
    )
}
