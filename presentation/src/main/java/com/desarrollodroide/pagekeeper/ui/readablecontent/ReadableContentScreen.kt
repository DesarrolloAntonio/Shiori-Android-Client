package com.desarrollodroide.pagekeeper.ui.readablecontent

import android.content.Intent
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog

@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadableContentScreen(
    readableContentViewModel: ReadableContentViewModel,
    onBack: () -> Unit,
    bookmarkUrl: String,
    bookmarkId: Int,
    openUrlInBrowser: (String) -> Unit,
    bookmarkDate: String,
    bookmarkTitle: String,
    isRtl: Boolean,
    /**
     * True when this is the detail pane of the two pane feed rather than a whole screen.
     *
     * The pane sits inside the feed's scaffold, which has already consumed the status bar inset.
     * This screen's own scaffold does not know that and applies it a second time, which is where
     * the band of empty space above the app bar came from. It also means the bar's title is one
     * of two headings stacked on top of each other, so in a pane the bar keeps only its close
     * button.
     */
    embeddedInPane: Boolean = false,
) {
    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        readableContentViewModel.load(bookmarkId = bookmarkId, bookmarkUrl = bookmarkUrl)
    }

    val themeMode by readableContentViewModel.themeMode.collectAsState()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val themeCss = if (isDarkTheme) DARK_THEME_CSS else LIGHT_THEME_CSS
    val directionCss = if (isRtl) RTL_CSS else LTR_CSS

    val readableContentState by readableContentViewModel.readableContentState.collectAsState()

    Scaffold(
        // No app bar in a pane: the header below carries the close button instead, and a 64dp bar
        // holding one arrow is a lot of a pane's height to spend on that.
        topBar = { if (!embeddedInPane) ReaderTopBar(onBack = onBack) },
        // The feed's scaffold has already inset this pane below the status bar. Left at the
        // default, this scaffold insets it again and the header floats in a band of nothing.
        contentWindowInsets = if (embeddedInPane) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxWidth()) {
            if (readableContentState.isLoading) {
                InfiniteProgressDialog(onDismissRequest = {})
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        TopSection(
                            title = bookmarkTitle,
                            date = bookmarkDate,
                            onClick = { openUrlInBrowser(bookmarkUrl) },
                            onClose = if (embeddedInPane) onBack else null,
                        )
                    }
                    item {
                        readableContentState.error?.let { error ->
                            ErrorView(errorMessage = error)
                        } ?: readableContentState.data?.let { readableMessage ->
                            // The stylesheet is part of the document now instead of being injected afterwards
                            // with evaluateJavascript, which is what allows JavaScript to be turned off below.
                            val styledHtml = buildString {
                                append("<!DOCTYPE html><html><head>")
                                append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                                append("<style>img{max-width:100%;height:auto;}")
                                append(directionCss)
                                append(themeCss)
                                append("</style></head><body>")
                                append(readableMessage.html)
                                append("</body></html>")
                            }
                            AndroidView(factory = { context ->
                                WebView(context).apply {
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            request?.url?.let { url ->
                                                context.startActivity(Intent(Intent.ACTION_VIEW, url))
                                                return true
                                            }
                                            return false
                                        }
                                    }
                                    // This renders HTML that came from the server, over cleartext in most self
                                    // hosted setups. Reading an article needs no scripting, no local file access
                                    // and no content providers, so none of them are enabled.
                                    settings.javaScriptEnabled = false
                                    settings.allowFileAccess = false
                                    settings.allowContentAccess = false
                                    setBackgroundColor(if (isDarkTheme) 0xFF121212.toInt() else 0xFFFFFFFF.toInt())
                                    loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Content", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

private const val DARK_THEME_CSS = """
    body {
        background-color: #121212;
        color: #ffffff;
    }
    a {
        color: #bb86fc;
    }
"""

private const val LIGHT_THEME_CSS = """
    body {
        background-color: #ffffff;
        color: #000000;
    }
    a {
        color: #1a0dab;
    }
"""

private const val RTL_CSS = """
    body {
        direction: rtl;
        text-align: right;
    }
"""

private const val LTR_CSS = """
    body {
        direction: ltr;
        text-align: left;
    }
"""
