package com.desarrollodroide.pagekeeper.ui.readablecontent

import android.content.Intent
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.viewinterop.AndroidView
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
    bookmarkTitle: String
) {
    BackHandler {
        onBack()
    }
    LaunchedEffect(Unit) {
        readableContentViewModel.loadInitialData()
        readableContentViewModel.getBookmarkReadableContent(bookmarkId)
    }
    val readableContentState = readableContentViewModel.readableContentState.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Content", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            if (readableContentState.value.isLoading) {
                InfiniteProgressDialog(onDismissRequest = {})
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    item {
                        TopSection(
                            title = bookmarkTitle,
                            date = bookmarkDate,
                            onClick = { openUrlInBrowser.invoke(bookmarkUrl) }
                        )
                    }
                    item {
                        if (readableContentState.value.error != null) {
                            ErrorView(errorMessage = readableContentState.value.error ?: "Error getting readable content")
                        } else {
                            readableContentState.value.data?.let { readableMessage ->
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
                                        loadDataWithBaseURL(
                                            null,
                                            readableMessage.html,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}


