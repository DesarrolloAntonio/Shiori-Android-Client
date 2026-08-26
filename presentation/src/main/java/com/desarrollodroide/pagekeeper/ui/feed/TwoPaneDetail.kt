package com.desarrollodroide.pagekeeper.ui.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.desarrollodroide.pagekeeper.extensions.isRTLText
import com.desarrollodroide.pagekeeper.ui.components.TwoPaneEmptyDetail
import com.desarrollodroide.pagekeeper.ui.readablecontent.ReadableContentScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Right hand pane of the two pane feed: the article for whichever bookmark is open.
 *
 * Reuses ReadableContentScreen rather than a second reader. Its own BackHandler becomes "close the
 * article", which is what back should do when the list is still on screen beside it.
 */
@RequiresApi(Build.VERSION_CODES.N)
@Composable
internal fun TwoPaneDetail(
    bookmarkId: Int?,
    feedViewModel: FeedViewModel,
    openUrlInBrowser: (String) -> Unit,
    onClose: () -> Unit,
) {
    if (bookmarkId == null) {
        TwoPaneEmptyDetail()
        return
    }
    val bookmark by feedViewModel.currentBookmark.collectAsState()
    LaunchedEffect(bookmarkId) { feedViewModel.loadBookmarkById(bookmarkId) }

    bookmark?.takeIf { it.id == bookmarkId }?.let {
        // Keyed so that picking a second bookmark builds a fresh reader. Without it the screen's
        // LaunchedEffect(Unit) never runs again and the pane keeps the first article.
        key(bookmarkId) {
            ReadableContentScreen(
                readableContentViewModel = koinViewModel(),
                bookmarkId = bookmarkId,
                bookmarkUrl = it.url,
                onBack = onClose,
                openUrlInBrowser = openUrlInBrowser,
                bookmarkDate = it.modified,
                bookmarkTitle = it.title,
                isRtl = it.title.isRTLText() || it.excerpt.isRTLText(),
                embeddedInPane = true,
            )
        }
    }
}
