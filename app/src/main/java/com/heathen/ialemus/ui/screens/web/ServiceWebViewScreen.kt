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
import androidx.compose.foundation.clickable
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
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.theme.screenHorizontalPadding
import com.heathen.ialemus.ui.util.openUrlInBrowser
import kotlinx.coroutines.delay

private enum class WebLoadState(val chipLabel: String) {
    IDLE("IDLE"),
    LOADING("LOADING"),
    LOADED("LOADED"),
    WARNING("WARNING"),
    ERROR("ERROR"),
}

private const val RENDER_TIMEOUT_MS = 20_000L
private const val LOAD_PROGRESS_DONE = 100

private const val MOBILE_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

/** Read-only DOM probe — advisory for MeTube only; never blocks slskd. */
private const val BLANK_PROBE_JS =
    "(function(){var b=document.body;if(!b)return'0';" +
        "if(b.children&&b.children.length>2)return'1';" +
        "if(b.innerText&&b.innerText.trim().length>8)return'1';return'0';})()"

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
    var mainFrameError by remember(loadUrl) { mutableStateOf<String?>(null) }
    var httpStatus by remember(loadUrl) { mutableStateOf<Int?>(null) }
    var currentUrl by remember(loadUrl) { mutableStateOf(loadUrl) }
    var pageTitle by remember(loadUrl) { mutableStateOf<String?>(null) }
    var pageLoaded by remember(loadUrl) { mutableStateOf(false) }
    var renderTimeout by remember(loadUrl) { mutableStateOf(false) }
    var blankContentSuspected by remember(loadUrl) { mutableStateOf(false) }
    var subresourceErrorCount by remember(loadUrl) { mutableIntStateOf(0) }
    var diagnosticsExpanded by remember { mutableStateOf(false) }
    var useDesktopMode by remember(loadUrl) {
        mutableStateOf(state.serviceKind == DockerWebService.NAS_UI)
    }
    var reloadKey by remember(loadUrl) { mutableIntStateOf(0) }
    var loadSession by remember(loadUrl) { mutableIntStateOf(0) }

    fun resetLoadSession() {
        pageLoaded = false
        renderTimeout = false
        blankContentSuspected = false
        mainFrameError = null
        httpStatus = null
        subresourceErrorCount = 0
        loadState = WebLoadState.LOADING
        loadProgress = 0
    }

    fun markPageUsable(title: String? = pageTitle) {
        pageLoaded = true
        renderTimeout = false
        blankContentSuspected = false
        if (mainFrameError == null) {
            loadState = WebLoadState.LOADED
        }
        title?.takeIf { it.isNotBlank() }?.let { pageTitle = it }
    }

    LaunchedEffect(loadUrl, reloadKey) {
        loadSession++
        val session = loadSession
        pageLoaded = false
        renderTimeout = false
        blankContentSuspected = false
        mainFrameError = null
        loadState = WebLoadState.LOADING
        delay(RENDER_TIMEOUT_MS)
        if (session == loadSession && !pageLoaded && mainFrameError == null && loadProgress < LOAD_PROGRESS_DONE) {
            renderTimeout = true
            loadState = WebLoadState.WARNING
        }
    }

    val showCompactWarning = !pageLoaded && mainFrameError == null &&
        (renderTimeout || (blankContentSuspected && isMeTube))
    val statusChip = when {
        mainFrameError != null -> WebLoadState.ERROR
        showCompactWarning -> WebLoadState.WARNING
        pageLoaded || loadState == WebLoadState.LOADED -> WebLoadState.LOADED
        loadState == WebLoadState.LOADING -> WebLoadState.LOADING
        else -> loadState
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = state.serviceName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.accentActive,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    HudStatusChip(
                        label = statusChip.chipLabel,
                        highlighted = statusChip == WebLoadState.LOADED,
                        warning = statusChip == WebLoadState.WARNING || statusChip == WebLoadState.ERROR,
                    )
                }
                Text(
                    text = pageTitle?.takeIf { it.isNotBlank() } ?: currentUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = {
                resetLoadSession()
                reloadKey++
                webView?.reload()
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = tokens.glowColor)
            }
            IconButton(onClick = { openUrlInBrowser(context, currentUrl) }) {
                Icon(Icons.Filled.OpenInBrowser, contentDescription = "Open external browser", tint = tokens.glowColor)
            }
            Text(
                text = "Diag",
                style = MaterialTheme.typography.labelSmall,
                color = if (diagnosticsExpanded) tokens.accentActive else tokens.textMuted,
                modifier = Modifier
                    .clickable { diagnosticsExpanded = !diagnosticsExpanded }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            )
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
                    resetLoadSession()
                    reloadKey++
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = tokens.accentActive,
                    checkedTrackColor = tokens.accentActive.copy(alpha = 0.35f),
                ),
            )
        }

        if (loadState == WebLoadState.LOADING && loadProgress < LOAD_PROGRESS_DONE && !pageLoaded) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = tokens.accentActive,
                trackColor = tokens.hudBorderColor.copy(alpha = 0.3f),
            )
        }

        if (showCompactWarning && mainFrameError == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPad, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isMeTube) {
                        "MeTube may not have rendered in WebView."
                    } else {
                        "Page slow to load — tap Diag for details."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.warningColor,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Reload",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.accentActive,
                    modifier = Modifier.clickable {
                        resetLoadSession()
                        reloadKey++
                    },
                )
            }
        }

        if (diagnosticsExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPad, vertical = 4.dp)
                    .background(tokens.panelOverlay.copy(alpha = 0.92f))
                    .padding(10.dp),
            ) {
                Text("DIAGNOSTICS", style = MaterialTheme.typography.labelSmall, color = tokens.accentActive)
                Text("Requested: $loadUrl", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                Text("Current: $currentUrl", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                Text("Progress: $loadProgress%", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                Text("Page loaded: $pageLoaded", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                Text("Render timeout: $renderTimeout", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                pageTitle?.let {
                    Text("Title: $it", style = MaterialTheme.typography.bodySmall, color = tokens.textMuted)
                }
                Text(
                    text = "UA: ${if (useDesktopMode) "Desktop" else "Mobile"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted,
                )
                if (subresourceErrorCount > 0) {
                    Text(
                        text = "Subresource warnings: $subresourceErrorCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textMuted,
                    )
                }
                mainFrameError?.let {
                    Text("Main frame error: $it", style = MaterialTheme.typography.bodySmall, color = tokens.warningColor)
                }
                if (blankContentSuspected) {
                    Text("Blank content suspected (MeTube probe).", style = MaterialTheme.typography.bodySmall, color = tokens.warningColor)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudButton(
                        label = "Reload",
                        onClick = { resetLoadSession(); reloadKey++ },
                        modifier = Modifier.weight(1f),
                        accent = HudButtonAccent.Neutral,
                    )
                    HudButton(
                        label = "Clear cache",
                        onClick = {
                            webView?.clearCache(true)
                            resetLoadSession()
                            reloadKey++
                        },
                        modifier = Modifier.weight(1f),
                        accent = HudButtonAccent.Neutral,
                    )
                }
                HudButton(
                    label = "Open external browser",
                    onClick = { openUrlInBrowser(context, currentUrl) },
                    modifier = Modifier.padding(top = 6.dp),
                    accent = HudButtonAccent.Neutral,
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = horizontalPad)
                .padding(bottom = 4.dp)
                .background(Color.White),
        ) {
            if (mainFrameError == null) {
                HudServiceWebView(
                    url = loadUrl,
                    useDesktopUserAgent = useDesktopMode,
                    reloadKey = reloadKey,
                    runBlankProbe = isMeTube,
                    onWebViewReady = { webView = it },
                    onPageStarted = {
                        loadState = WebLoadState.LOADING
                        pageLoaded = false
                        renderTimeout = false
                    },
                    onPageFinished = { url, title ->
                        markPageUsable(title)
                        url?.let { currentUrl = it }
                    },
                    onProgressChanged = { progress ->
                        loadProgress = progress
                        if (progress >= LOAD_PROGRESS_DONE && mainFrameError == null) {
                            markPageUsable()
                        }
                    },
                    onUrlChanged = { currentUrl = it },
                    onTitleChanged = { title ->
                        pageTitle = title
                        if (title.isNotBlank() && mainFrameError == null) {
                            markPageUsable(title)
                        }
                    },
                    onBlankContentSuspected = {
                        if (isMeTube && !pageLoaded) {
                            blankContentSuspected = true
                        }
                    },
                    onMainFrameError = { message, status ->
                        mainFrameError = message
                        httpStatus = status
                        loadState = WebLoadState.ERROR
                        renderTimeout = false
                    },
                    onSubresourceError = {
                        subresourceErrorCount++
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (loadState == WebLoadState.LOADING && loadProgress < 15 && !pageLoaded && mainFrameError == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = tokens.accentActive,
                )
            }

            if (mainFrameError != null) {
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
                        text = mainFrameError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.warningColor,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    HudButton(
                        label = "Retry",
                        onClick = {
                            resetLoadSession()
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HudServiceWebView(
    url: String,
    useDesktopUserAgent: Boolean,
    reloadKey: Int,
    runBlankProbe: Boolean,
    onWebViewReady: (WebView) -> Unit,
    onPageStarted: () -> Unit,
    onPageFinished: (String?, String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onBlankContentSuspected: () -> Unit,
    onMainFrameError: (String, Int?) -> Unit,
    onSubresourceError: () -> Unit,
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
                        onPageStarted()
                        url?.let(onUrlChanged)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        val title = view?.title
                        onPageFinished(url, title)
                        if (runBlankProbe) {
                            view?.evaluateJavascript(BLANK_PROBE_JS) { result ->
                                if ((result == "\"0\"" || result == "0") && title.isNullOrBlank()) {
                                    onBlankContentSuspected()
                                }
                            }
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onMainFrameError(
                                "HTTP ${errorResponse?.statusCode ?: 0}: page returned an error.",
                                errorResponse?.statusCode,
                            )
                        } else {
                            onSubresourceError()
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            onMainFrameError(
                                "Error ${error?.errorCode ?: "?"}: ${error?.description?.toString() ?: "Could not load page."}",
                                error?.errorCode,
                            )
                        } else {
                            onSubresourceError()
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        title?.takeIf { it.isNotBlank() }?.let(onTitleChanged)
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
