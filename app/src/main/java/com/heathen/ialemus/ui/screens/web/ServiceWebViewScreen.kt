package com.heathen.ialemus.ui.screens.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.heathen.ialemus.BuildConfig
import com.heathen.ialemus.core.network.ServiceUrlValidator
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudButtonAccent
import com.heathen.ialemus.ui.components.HudCollapsiblePanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser
import kotlinx.coroutines.delay

private enum class WebLoadState(val label: String) {
    IDLE("Idle"),
    LOADING("Loading"),
    LOADED("Loaded"),
    ERROR("Error"),
    RENDER_TIMEOUT("Render timeout"),
}

private const val RENDER_TIMEOUT_MS = 15_000L

private const val MOBILE_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

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
    val isMeTube = state.serviceKind == DockerWebService.METUBE ||
        state.serviceName.contains("MeTube", ignoreCase = true)

    var webView by remember { mutableStateOf<WebView?>(null) }
    var loadState by remember(loadUrl) { mutableStateOf(WebLoadState.LOADING) }
    var loadProgress by remember(loadUrl) { mutableIntStateOf(0) }
    var pageError by remember(loadUrl) { mutableStateOf<String?>(null) }
    var httpStatus by remember(loadUrl) { mutableStateOf<Int?>(null) }
    var currentUrl by remember(loadUrl) { mutableStateOf(loadUrl) }
    var pageTitle by remember(loadUrl) { mutableStateOf<String?>(null) }
    var showRenderWarning by remember(loadUrl) { mutableStateOf(false) }
    var blankContentDetected by remember(loadUrl) { mutableStateOf(false) }
    var diagnosticsExpanded by remember { mutableStateOf(false) }
    var useDesktopMode by remember(loadUrl) {
        mutableStateOf(state.serviceKind == DockerWebService.NAS_UI)
    }
    var reloadKey by remember(loadUrl) { mutableIntStateOf(0) }

    LaunchedEffect(loadUrl, loadState, loadProgress) {
        if (loadState == WebLoadState.LOADED && loadProgress >= 100) {
            delay(RENDER_TIMEOUT_MS)
            if (pageError == null && !blankContentDetected) {
                showRenderWarning = true
                loadState = WebLoadState.RENDER_TIMEOUT
            }
        }
    }

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
                    text = buildStatusLine(loadState, loadProgress, httpStatus, pageError, pageTitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (loadState) {
                        WebLoadState.ERROR, WebLoadState.RENDER_TIMEOUT -> tokens.warningColor
                        WebLoadState.LOADED -> tokens.successAccent
                        else -> tokens.textMuted
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { webView?.reload() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = tokens.glowColor)
            }
            IconButton(onClick = { openUrlInBrowser(context, currentUrl) }) {
                Icon(Icons.Filled.OpenInBrowser, contentDescription = "Open external browser", tint = tokens.glowColor)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPad, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Desktop mode", style = MaterialTheme.typography.labelSmall, color = tokens.textMuted)
            Switch(
                checked = useDesktopMode,
                onCheckedChange = {
                    useDesktopMode = it
                    reloadKey++
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = tokens.accentActive,
                    checkedTrackColor = tokens.accentActive.copy(alpha = 0.35f),
                ),
            )
        }

        if (loadState == WebLoadState.LOADING && loadProgress < 100) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = tokens.accentActive,
                trackColor = tokens.hudBorderColor.copy(alpha = 0.3f),
            )
        }

        if (showRenderWarning && pageError == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPad, vertical = 4.dp),
            ) {
                HudStatusChip(label = if (isMeTube) "WEBVIEW RENDER ISSUE" else "RENDER WARNING", warning = true)
                Text(
                    text = if (isMeTube) {
                        "MeTube is reachable, but this Android WebView did not render its interface. " +
                            "Try desktop mode, reload, or use external browser for now."
                    } else {
                        "Service reachable, but WebView did not render. Try desktop mode or external browser."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.warningColor,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HudButton(
                        label = "Reload",
                        onClick = { reloadKey++; showRenderWarning = false; loadState = WebLoadState.LOADING },
                        modifier = Modifier.weight(1f),
                        accent = HudButtonAccent.Neutral,
                    )
                    HudButton(
                        label = "External browser",
                        onClick = { openUrlInBrowser(context, loadUrl) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        HudCollapsiblePanel(
            title = "Web Diagnostics",
            sectionTag = "DIAGNOSTICS",
            subtitle = "URL, load state, errors, and cache controls.",
            expanded = diagnosticsExpanded,
            onToggle = { diagnosticsExpanded = !diagnosticsExpanded },
            statusLabel = loadState.label.uppercase(),
            modifier = Modifier.padding(horizontal = horizontalPad),
        ) {
            Text("Requested URL: $loadUrl", style = MaterialTheme.typography.bodySmall, color = tokens.textPrimary)
            Text("Current URL: $currentUrl", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
            Text("State: ${loadState.label}", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
            Text("Progress: $loadProgress%", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
            pageTitle?.let {
                Text("Title: $it", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
            }
            Text(
                text = "User-Agent: ${if (useDesktopMode) "Desktop Chrome" else "Mobile Chrome"}",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted,
            )
            pageError?.let {
                Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = tokens.warningColor)
            }
            httpStatus?.let {
                Text("HTTP: $it", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
            }
            if (blankContentDetected) {
                Text("Blank content detected after load.", style = MaterialTheme.typography.bodySmall, color = tokens.warningColor)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HudButton(
                    label = "Clear cache",
                    onClick = {
                        webView?.clearCache(true)
                        reloadKey++
                    },
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Neutral,
                )
                HudButton(
                    label = "Reload",
                    onClick = { reloadKey++; showRenderWarning = false },
                    modifier = Modifier.weight(1f),
                    accent = HudButtonAccent.Neutral,
                )
            }
            HudButton(
                label = "Open external browser",
                onClick = { openUrlInBrowser(context, currentUrl) },
                modifier = Modifier.padding(top = 8.dp),
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
                    useDesktopUserAgent = useDesktopMode,
                    reloadKey = reloadKey,
                    onWebViewReady = { webView = it },
                    onLoadStateChanged = { loadState = it },
                    onProgressChanged = { loadProgress = it },
                    onUrlChanged = { currentUrl = it },
                    onTitleChanged = { pageTitle = it },
                    onBlankContent = {
                        blankContentDetected = true
                        showRenderWarning = true
                        loadState = WebLoadState.RENDER_TIMEOUT
                    },
                    onError = { message, status ->
                        pageError = message
                        httpStatus = status
                        loadState = WebLoadState.ERROR
                        showRenderWarning = false
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
                        text = pageError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.warningColor,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    HudButton(
                        label = "Retry",
                        onClick = {
                            pageError = null
                            httpStatus = null
                            loadState = WebLoadState.LOADING
                            reloadKey++
                        },
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
    title: String?,
): String {
    val titleSuffix = title?.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
    return when (loadState) {
        WebLoadState.LOADING -> "Loading… $progress%$titleSuffix"
        WebLoadState.LOADED -> "Loaded$titleSuffix" + (httpStatus?.let { " · HTTP $it" }.orEmpty())
        WebLoadState.RENDER_TIMEOUT -> "Render timeout — try external browser$titleSuffix"
        WebLoadState.ERROR -> error ?: "Error"
        WebLoadState.IDLE -> "Idle"
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HudServiceWebView(
    url: String,
    useDesktopUserAgent: Boolean,
    reloadKey: Int,
    onWebViewReady: (WebView) -> Unit,
    onLoadStateChanged: (WebLoadState) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onBlankContent: () -> Unit,
    onError: (String, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val userAgent = if (useDesktopUserAgent) DESKTOP_USER_AGENT else MOBILE_USER_AGENT
    var lastReloadKey by remember { mutableIntStateOf(0) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            CookieManager.getInstance().setAcceptCookie(true)
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(android.graphics.Color.WHITE)
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                @Suppress("DEPRECATION")
                settings.databaseEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = false
                settings.allowContentAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.setSupportZoom(true)
                settings.userAgentString = userAgent
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val scheme = request?.url?.scheme?.lowercase()
                        return scheme != "http" && scheme != "https"
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLoadStateChanged(WebLoadState.LOADING)
                        url?.let(onUrlChanged)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadStateChanged(WebLoadState.LOADED)
                        url?.let(onUrlChanged)
                        view?.title?.let(onTitleChanged)
                        view?.evaluateJavascript(
                            "(function(){var b=document.body;return b&&b.innerText&&b.innerText.trim().length>20?'1':'0';})()",
                        ) { result ->
                            if (result == "\"0\"" || result == "0") {
                                onBlankContent()
                            }
                        }
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

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        title?.let(onTitleChanged)
                    }
                }
                onWebViewReady(this)
                loadUrl(url)
            }
        },
        update = { view ->
            if (view.settings.userAgentString != userAgent) {
                view.settings.userAgentString = userAgent
            }
            if (reloadKey != lastReloadKey) {
                lastReloadKey = reloadKey
                view.loadUrl(url)
                return@AndroidView
            }
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
