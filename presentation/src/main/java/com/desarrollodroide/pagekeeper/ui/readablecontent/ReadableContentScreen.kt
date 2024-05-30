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
    isRtl: Boolean
) {
    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        readableContentViewModel.loadInitialData()
        readableContentViewModel.getBookmarkReadableContent(bookmarkId)
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Content", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
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
                            onClick = { openUrlInBrowser(bookmarkUrl) }
                        )
                    }
                    item {
                        readableContentState.error?.let { error ->
                            ErrorView(errorMessage = error)
                        } ?: readableContentState.data?.let { readableMessage ->
                            AndroidView(factory = { context ->
                                WebView(context).apply {
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            val css = """
                                                (function() {
                                                    var style = document.createElement('style');
                                                    style.innerHTML = `
                                                        img {
                                                            max-width: 100%;
                                                            height: auto;
                                                        }
                                                        $directionCss
                                                        $themeCss
                                                    `;
                                                    document.head.appendChild(style);
                                                })();
                                            """.trimIndent()
                                            view?.evaluateJavascript(css, null)
                                        }

                                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                            request?.url?.let { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, url)
                                                context.startActivity(intent)
                                                return true
                                            }
                                            return false
                                        }
                                    }
                                    settings.javaScriptEnabled = true
                                    loadDataWithBaseURL(null, readableMessage.html, "text/html", "UTF-8", null)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
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
