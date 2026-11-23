package com.desarrollodroide.pagekeeper.ui.archive

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayInputStream

/**
 * The copy of a page the server archived when it was saved (issue #66).
 *
 * JavaScript stays off whatever the page looks like without it. An archive is someone else's page
 * served from the user's own server, so its scripts would run with the session cookie and could
 * call the API as the user. Only this bookmark's archive files load in here; see
 * [classifyArchiveNavigation].
 */
@Composable
fun ArchiveScreen(
    bookmarkId: Int,
    bookmarkTitle: String,
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = koinViewModel(),
) {
    BackHandler { onBack() }
    val session by viewModel.session.collectAsStateWithLifecycle()
    var loading by remember { mutableStateOf(true) }
    var problem by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { ArchiveTopBar(title = bookmarkTitle, loading = loading && problem == null, onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            session?.let { current ->
                ArchiveWebView(
                    session = current,
                    bookmarkId = bookmarkId,
                    onLoaded = { loading = false },
                    onProblem = { problem = it },
                )
            }
            problem?.let { message ->
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveTopBar(title: String, loading: Boolean, onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.statusBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Archived copy",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ArchiveWebView(
    session: ArchiveViewModel.Session,
    bookmarkId: Int,
    onLoaded: () -> Unit,
    onProblem: (String) -> Unit,
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.domStorageEnabled = false
            // Archived pages are laid out for a desktop browser: fit them, and let them be zoomed.
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
        }
    }

    DisposableEffect(Unit) {
        webView.webViewClient = object : WebViewClient() {

            fun classify(url: String) = classifyArchiveNavigation(url, session.serverUrl, bookmarkId)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return when (classify(url)) {
                    ArchiveNavigation.InViewer -> false
                    ArchiveNavigation.External -> {
                        if (!request.isRedirect) {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: ActivityNotFoundException) {
                                // Nothing on the device opens it; staying on the archive is fine.
                            }
                        }
                        true
                    }
                    ArchiveNavigation.Blocked -> {
                        if (request.isForMainFrame) onProblem(blockedMessage(url))
                        true
                    }
                }
            }

            // Subresources never reach shouldOverrideUrlLoading. Without this an archived page
            // could still load anything else on the user's server, with the session attached.
            // Only http(s): data: and blob: images inside the archive are not the server's.
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                if (request.url.scheme in setOf("http", "https") &&
                    classify(request.url.toString()) == ArchiveNavigation.Blocked
                ) {
                    WebResourceResponse("text/plain", "utf-8", 403, "Forbidden", emptyMap(), ByteArrayInputStream(ByteArray(0)))
                } else {
                    null
                }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // A redirect to the login page arrives here without passing shouldOverrideUrlLoading
                // on some WebView versions.
                if (classify(url) == ArchiveNavigation.Blocked) {
                    view.stopLoading()
                    onProblem(blockedMessage(url))
                }
            }

            override fun onPageFinished(view: WebView, url: String) = onLoaded()

            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                if (!request.isForMainFrame) return
                onProblem(
                    when (errorResponse.statusCode) {
                        404 -> "The server has no archive for this bookmark."
                        401, 403 -> SESSION_PROBLEM
                        else -> "The server answered HTTP ${errorResponse.statusCode}."
                    }
                )
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) onProblem("Couldn't load the archive: ${error.description}")
            }
        }
        ArchiveWebData.putSession(session.serverUrl, session.token)
        webView.loadUrl(archiveFileUrl(session.serverUrl, bookmarkId))

        onDispose {
            webView.stopLoading()
            webView.clearCache(true)
            ArchiveWebData.removeSession()
            webView.destroy()
        }
    }

    AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
}

/** Shiori 1.8 sends a refused session to its root with `?dst=`, older versions to `/login`. */
private fun blockedMessage(url: String): String =
    if (url.contains("dst=") || url.contains("/login")) SESSION_PROBLEM else "That link can't be opened here."

private const val SESSION_PROBLEM =
    "The server didn't accept your session. Log in again to see the archive."
