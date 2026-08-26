package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The full bookmark card lays out an action row whose item count varies per bookmark. Measuring it
 * inside a LazyColumn crashed with "maxWidth must be >= than minWidth" from the action row's
 * measure policy, which took down the feed as soon as it was drawn.
 */
@RunWith(AndroidJUnit4::class)
class BookmarkItemLayoutTest {

    @get:Rule
    val rule = createComposeRule()

    private val noopActions = BookmarkActions(
        onClickEdit = {},
        onClickDelete = {},
        onClickShare = {},
        onClickCategory = {},
        onClickBookmark = {},
        onClickEpub = {},
        onClickSync = {},
    )

    private fun bookmark(hasEbook: Boolean, id: Int) = Bookmark.mock().copy(
        id = id,
        title = "A bookmark title",
        hasEbook = hasEbook,
    )

    @Test
    fun fullBookmarkCardLaysOutInsideALazyColumn() {
        rule.setContent {
            ShioriTheme {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(3) { index ->
                        BookmarkItem(
                            getBookmark = { bookmark(hasEbook = index % 2 == 0, id = 100 + index) },
                            serverURL = "",
                            xSessionId = "",
                            token = "",
                            actions = noopActions,
                            viewType = BookmarkViewType.FULL,
                        )
                    }
                }
            }
        }
        // Reaching an assertion at all is the point: the crash happened during measurement, so a
        // failure here used to be an IllegalArgumentException rather than a failed check.
        rule.onAllNodesWithText("A bookmark title")[0].assertIsDisplayed()
    }

    @Test
    fun fullBookmarkCardLaysOutOnANarrowScreen() {
        rule.setContent {
            ShioriTheme {
                LazyColumn(modifier = Modifier.width(280.dp)) {
                    items(1) {
                        BookmarkItem(
                            getBookmark = { bookmark(hasEbook = true, id = 1) },
                            serverURL = "",
                            xSessionId = "",
                            token = "",
                            actions = noopActions,
                            viewType = BookmarkViewType.FULL,
                        )
                    }
                }
            }
        }
        rule.onNodeWithText("A bookmark title").assertIsDisplayed()
    }
}
