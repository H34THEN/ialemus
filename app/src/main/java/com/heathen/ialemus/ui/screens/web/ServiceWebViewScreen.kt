package com.heathen.ialemus.ui.screens.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.heathen.ialemus.core.network.ServiceUrlValidator
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser

private enum class WebLoadState(val label: String) {
    LOADING("Loading"),
    LOADED("Loaded"),
    ERROR("Error"),
}

@Composable
fun ServiceWebViewScreen(
    state: ServiceWebViewState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalIalemusTokens.current
    val context = LocalContext.current
    val horizontalPad = screenHorizontalPadding()
    val loadUrl = remember(state.url) { ServiceUrlValidator.normalizeForLoad(state.url) }

    var webView by remember { mutableStateOf<WebView?>(null) }
    var loadState by remember { mutableStateOf(WebLoadState.LOADING) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var httpStatus by remember { mutableStateOf<Int?>(null) }
    var currentUrl by remember(loadUrl) { mutableStateOf(loadUrl) }

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
            .background(tokens.surfaceDeep),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPad, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                Text(
                    text = buildStatusLine(loadState, loadProgress, httpStatus, pageError),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (loadState) {
                        WebLoadState.ERROR -> tokens.warningColor
                        WebLoadState.LOADED -> tokens.successAccent
                        WebLoadState.LOADING -> tokens.textMuted
                    },
                    maxLines = 2,
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

        if (loadState == WebLoadState.LOADING && loadProgress < 100) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = tokens.accentActive,
                trackColor = tokens.hudBorderColor.copy(alpha = 0.3f),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = horizontalPad)
                .padding(bottom = 4.dp)
                .background(Color.White),
        ) {
            if (pageError == null) {
                HudServiceWebView(
                    url = loadUrl,
                    onWebViewReady = { webView = it },
                    onLoadStateChanged = { loadState = it },
                    onProgressChanged = { loadProgress = it },
                    onUrlChanged = { currentUrl = it },
                    onError = { message, status ->
                        pageError = message
                        httpStatus = status
                        loadState = WebLoadState.ERROR
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (loadState == WebLoadState.LOADING && loadProgress < 15 && pageError == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = tokens.accentActive,
                )
            }

            if (pageError != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tokens.panelOverlay.copy(alpha = 0.95f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    HudStatusChip(label = "LOAD FAILED", warning = true)
                    Text(
                        text = "${state.serviceName} could not load.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textPrimary,
                    )
                    Text(
                        text = currentUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = pageError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.warningColor,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    httpStatus?.let { status ->
                        Text(
                            text = "HTTP status: $status",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textMuted,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    HudButton(
                        label = "Retry",
                        onClick = {
                            pageError = null
                            httpStatus = null
                            loadState = WebLoadState.LOADING
                            webView?.reload()
                        },
                        accent = HudButtonAccent.Primary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    HudButton(
                        label = "Open external browser",
                        onClick = { openUrlInBrowser(context, loadUrl) },
                        accent = HudButtonAccent.Neutral,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

private fun buildStatusLine(
    loadState: WebLoadState,
    progress: Int,
    httpStatus: Int?,
    error: String?,
): String {
    return when (loadState) {
        WebLoadState.LOADING -> "Status: Loading… $progress%"
        WebLoadState.LOADED -> {
            val statusSuffix = httpStatus?.let { " · HTTP $it" }.orEmpty()
            "Status: Loaded$statusSuffix"
        }
        WebLoadState.ERROR -> "Status: ${error ?: "Error"}"
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HudServiceWebView(
    url: String,
    onWebViewReady: (WebView) -> Unit,
    onLoadStateChanged: (WebLoadState) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onUrlChanged: (String) -> Unit,
    onError: (String, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(android.graphics.Color.WHITE)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = false
                settings.allowContentAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val target = request?.url?.toString() ?: return false
                        val scheme = request.url.scheme?.lowercase()
                        return if (scheme == "http" || scheme == "https") {
                            false
                        } else {
                            true
                        }
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLoadStateChanged(WebLoadState.LOADING)
                        url?.let(onUrlChanged)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadStateChanged(WebLoadState.LOADED)
                        url?.let(onUrlChanged)
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onError(
                                "HTTP ${errorResponse?.statusCode ?: 0}: page returned an error.",
                                errorResponse?.statusCode,
                            )
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onError(
                                "Error ${error?.errorCode ?: "?"}: ${error?.description?.toString() ?: "Could not load page."}",
                                error?.errorCode,
                            )
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                        if (newProgress >= 100) {
                            onLoadStateChanged(WebLoadState.LOADED)
                        }
                    }
                }
                onWebViewReady(this)
                loadUrl(url)
            }
        },
        update = { view ->
            val current = view.url ?: view.originalUrl
            if (current == null || !urlsMatch(current, url)) {
                view.loadUrl(url)
            }
        },
    )
}

private fun urlsMatch(current: String, target: String): Boolean {
    val normalizedCurrent = ServiceUrlValidator.normalizeForLoad(current)
    val normalizedTarget = ServiceUrlValidator.normalizeForLoad(target)
    return normalizedCurrent == normalizedTarget ||
        normalizedCurrent.trimEnd('/') == normalizedTarget.trimEnd('/')
}
