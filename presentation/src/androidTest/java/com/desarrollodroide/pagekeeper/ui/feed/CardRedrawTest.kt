package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
 * A card redraws when its bookmark changes under the same list key.
 *
 * The feed keys a card on id and modified. Check on the pending banner brings back the server's
 * scraped copy, and Shiori does not touch modified when it scrapes, so the key stays the same and
 * the card is recomposed rather than recreated. Each card view remembered the first bookmark it
 * was given, so Room held "Example Domain" while the card kept the url and the banner until the
 * app was relaunched (seen 2026-11-21, bookmark 119 on the NAS test server).
 */
@RunWith(AndroidJUnit4::class)
class CardRedrawTest {

    @get:Rule
    val rule = createComposeRule()

    /** What the create answered: the server has the bookmark and nothing on it yet. */
    private val justCreated = Bookmark.mock().copy(
        id = 119,
        url = "https://example.org/QA_redraw",
        title = "https://example.org/QA_redraw",
        excerpt = "",
        imageURL = "",
        hasContent = false,
        hasArchive = false,
        hasEbook = false,
        tags = emptyList(),
        createAt = "2026-11-21 13:54:00",
        modified = "2026-11-21 13:54:00",
    )

    /** What Check brought back: same id, same modified, now with the page's title. */
    private val scraped = justCreated.copy(
        title = "Example Domain",
        excerpt = "This domain is for use in documentation examples.",
    )

    private val noActions = BookmarkActions(
        onClickEdit = { },
        onClickDelete = { },
        onClickShare = { },
        onClickCategory = { },
        onClickBookmark = { },
        onClickEpub = { },
        onClickSync = { },
        onToggleSelection = { },
    )

    private fun showCard(viewType: BookmarkViewType): MutableState<Bookmark> {
        val row = mutableStateOf(justCreated)
        rule.setContent {
            ShioriTheme {
                // Read here and handed down as a plain value, the way FeedContent hands down the
                // paging item. A lambda that read the state itself would hide the bug.
                val bookmark = row.value
                BookmarkItem(
                    getBookmark = { bookmark },
                    serverURL = "http://test",
                    xSessionId = "",
                    token = "",
                    viewType = viewType,
                    actions = noActions,
                )
            }
        }
        rule.waitForIdle()
        return row
    }

    @Test
    fun aFullCardShowsWhatCheckBroughtBack() {
        val row = showCard(BookmarkViewType.FULL)
        rule.onNodeWithText(PENDING).assertIsDisplayed()

        row.value = scraped
        rule.waitForIdle()

        rule.onNodeWithText("Example Domain").assertIsDisplayed()
        rule.onAllNodesWithText(PENDING).assertCountEquals(0)
    }

    @Test
    fun aCompactCardShowsWhatCheckBroughtBack() {
        val row = showCard(BookmarkViewType.SMALL)
        rule.onNodeWithText(PENDING).assertIsDisplayed()

        row.value = scraped
        rule.waitForIdle()

        rule.onNodeWithText("Example Domain").assertIsDisplayed()
        rule.onAllNodesWithText(PENDING).assertCountEquals(0)
    }

    private companion object {
        const val PENDING = "Pending server processing"
    }
}
