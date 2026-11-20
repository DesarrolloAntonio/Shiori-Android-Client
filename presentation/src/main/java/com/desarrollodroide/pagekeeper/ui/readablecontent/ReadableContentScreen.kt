package com.desarrollodroide.pagekeeper.ui.readablecontent

import android.content.Intent
import android.content.ActivityNotFoundException
import android.widget.Toast
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.desarrollodroide.pagekeeper.extensions.shareText
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
     * the band of empty space above the app bar came from. In a pane there is no app bar either:
     * the header carries the close button instead.
     */
    embeddedInPane: Boolean = false,
) {
    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        readableContentViewModel.load(bookmarkId = bookmarkId, bookmarkUrl = bookmarkUrl)
    }

    val context = LocalContext.current
    val readableContentState by readableContentViewModel.readableContentState.collectAsState()
    val listState = rememberLazyListState()
    val progress by remember { derivedStateOf { readingProgress(listState) } }
    // Not persisted: a size for this reading, not a preference to manage in Settings.
    var textSizeStep by rememberSaveable { mutableIntStateOf(DefaultTextSizeStep) }

    Scaffold(
        topBar = {
            if (embeddedInPane) {
                ReadingProgress(progress = progress)
            } else {
                ReaderTopBar(onBack = onBack, progress = progress)
            }
        },
        // The feed's scaffold has already inset this pane below the status bar. Left at the
        // default, this scaffold insets it again and the header floats in a band of nothing.
        contentWindowInsets = if (embeddedInPane) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (readableContentState.isLoading) {
                InfiniteProgressDialog(onDismissRequest = {})
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    // Room for the floating toolbar, so the last lines can scroll clear of it.
                    contentPadding = PaddingValues(top = 8.dp, bottom = 104.dp),
                ) {
                    item {
                        TopSection(
                            title = bookmarkTitle,
                            date = bookmarkDate,
                            url = bookmarkUrl,
                            onClick = { openUrlInBrowser(bookmarkUrl) },
                            onClose = if (embeddedInPane) onBack else null,
                        )
                    }
                    item {
                        readableContentState.error?.let { error ->
                            ErrorView(errorMessage = error)
                        } ?: readableContentState.data?.let { readableMessage ->
                            ArticleWebView(
                                html = readableMessage.html,
                                isRtl = isRtl,
                                textZoom = TextZoomSteps[textSizeStep],
                            )
                        }
                    }
                }
            }
            ReaderToolbar(
                onTextSize = { textSizeStep = (textSizeStep + 1) % TextZoomSteps.size },
                textSizeDescription = "Text size, ${TextZoomSteps[textSizeStep]} percent",
                onShare = { context.shareText(bookmarkUrl) },
                onOpenOriginal = { openUrlInBrowser(bookmarkUrl) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            )
        }
    }
}

/** Back, in a tonal circle, and how far through the article the reader is. */
@Composable
private fun ReaderTopBar(onBack: () -> Unit, progress: Float) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
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
            }
            ReadingProgress(progress = progress)
        }
    }
}

/** The Expressive wavy indicator, filling as the article scrolls. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReadingProgress(progress: Float) {
    LinearWavyProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .semantics { contentDescription = "Reading progress" },
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

/** Text size, share and the original page, floating at the bottom within reach of a thumb. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReaderToolbar(
    onTextSize: () -> Unit,
    textSizeDescription: String,
    onShare: () -> Unit,
    onOpenOriginal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
    ) {
        IconButton(
            onClick = onTextSize,
            modifier = Modifier.semantics { contentDescription = textSizeDescription },
        ) {
            Icon(Icons.Filled.FormatSize, contentDescription = null)
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Outlined.Share, contentDescription = "Share")
        }
        IconButton(onClick = onOpenOriginal) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open original")
        }
    }
}

/**
 * The article, rendered from the server's HTML.
 *
 * The stylesheet is part of the document rather than injected afterwards with evaluateJavascript,
 * which is what allows JavaScript to stay off. It takes its colours from the theme, so the text
 * follows light, dark and dynamic colour like the rest of the app instead of a fixed white or
 * #121212 page. The text size goes through the WebView's own zoom, so changing it keeps the
 * reader's place instead of reloading the page.
 */
@Composable
private fun ArticleWebView(html: String, isRtl: Boolean, textZoom: Int) {
    val colors = MaterialTheme.colorScheme
    val document = remember(html, colors, isRtl) {
        buildString {
            append("<!DOCTYPE html><html><head>")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            append("<style>")
            append(readerCss(colors, isRtl))
            append("</style></head><body>")
            append(html)
            append("</body></html>")
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url?.let { url ->
                            // From develop (ca02e1b): a link with no app to open it used to crash.
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, url))
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
                            }
                            return true
                        }
                        return false
                    }
                }
                // This renders HTML that came from the server, over cleartext in most self hosted
                // setups. Reading an article needs no scripting, no local file access and no
                // content providers, so none of them are enabled.
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
            }
        },
        update = { webView ->
            webView.setBackgroundColor(colors.surface.toArgb())
            webView.settings.textZoom = textZoom
            // Only when the document itself changed: reloading on every recomposition would throw
            // the reader back to the top.
            if (webView.tag != document) {
                webView.tag = document
                webView.loadDataWithBaseURL(null, document, "text/html", "UTF-8", null)
            }
        },
    )
}

/**
 * How far through the article the list is, from 0 to 1.
 *
 * The article is one item, the second, so it is measured by how much of it has scrolled past the
 * top of the viewport against how much of it can.
 */
private fun readingProgress(state: LazyListState): Float {
    val info = state.layoutInfo
    val article = info.visibleItemsInfo.firstOrNull { it.index == 1 }
        ?: return if (state.firstVisibleItemIndex > 1) 1f else 0f
    val viewport = info.viewportEndOffset - info.viewportStartOffset
    val scrollable = (article.size - viewport).coerceAtLeast(1)
    val scrolled = (-article.offset).coerceIn(0, scrollable)
    return scrolled.toFloat() / scrollable
}

/** The article's typography: a serif to read, the app's heavy sans for headings, theme colours. */
private fun readerCss(colors: ColorScheme, isRtl: Boolean): String = """
    html, body { margin: 0; padding: 0; }
    body {
        background: ${colors.surface.hex()};
        color: ${colors.onSurface.hex()};
        font-family: Georgia, 'Noto Serif', serif;
        font-size: 18px;
        line-height: 1.65;
        padding: 0 24px;
        direction: ${if (isRtl) "rtl" else "ltr"};
        text-align: ${if (isRtl) "right" else "left"};
        overflow-wrap: break-word;
    }
    h1, h2, h3, h4, h5 {
        font-family: sans-serif;
        font-weight: 900;
        line-height: 1.2;
        letter-spacing: -0.01em;
    }
    a { color: ${colors.primary.hex()}; text-decoration-thickness: 2px; }
    img, video { max-width: 100%; height: auto; border-radius: 16px; }
    figure { margin: 24px 0; }
    figcaption { font-family: sans-serif; font-size: 14px; color: ${colors.onSurfaceVariant.hex()}; }
    blockquote {
        margin: 24px 0;
        padding: 16px 20px;
        border-radius: 24px;
        background: ${colors.tertiaryContainer.hex()};
        color: ${colors.onTertiaryContainer.hex()};
        font-family: sans-serif;
        font-weight: 700;
        line-height: 1.45;
    }
    code, pre {
        font-family: monospace;
        background: ${colors.surfaceContainerHigh.hex()};
        border-radius: 8px;
    }
    code { padding: 2px 6px; }
    pre { padding: 16px; border-radius: 16px; overflow-x: auto; }
    hr { border: 0; height: 2px; background: ${colors.outlineVariant.hex()}; margin: 32px 0; }
""".trimIndent()

private fun Color.hex(): String = String.format("#%06X", 0xFFFFFF and toArgb())

/** WebView text zoom steps, in percent. The text size button cycles through them. */
private val TextZoomSteps = listOf(90, 100, 115, 130)
private const val DefaultTextSizeStep = 1
