package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.ui.components.LocalFeedInListPane
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * What a card shows when the article is open beside it.
 *
 * In two panes the list is the narrowest column on screen and the pane next to it is showing the
 * whole article. Three lines of excerpt there are three lines of the same words, twice.
 */
@RunWith(AndroidJUnit4::class)
class ListPaneCardTest {

    @get:Rule
    val rule = createComposeRule()

    private val bookmark = Bookmark.mock().copy(
        id = 1,
        title = "Coroutines | Kotlin",
        excerpt = "Applications often need to perform multiple tasks at the same time.",
        imageURL = "",
        tags = emptyList(),
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

    private fun card(inListPane: Boolean) {
        rule.setContent {
            ShioriTheme {
                CompositionLocalProvider(LocalFeedInListPane provides inListPane) {
                    BookmarkItem(
                        getBookmark = { bookmark },
                        serverURL = "http://test",
                        xSessionId = "",
                        token = "",
                        viewType = BookmarkViewType.FULL,
                        actions = noActions,
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun theExcerptIsDroppedInsideTheListPane() {
        card(inListPane = true)

        rule.onNodeWithText(bookmark.title).assertIsDisplayed()
        rule.onAllNodesWithText(bookmark.excerpt).assertCountEquals(0)
    }

    @Test
    fun theExcerptIsKeptWhenTheFeedOwnsTheWindow() {
        card(inListPane = false)

        rule.onNodeWithText(bookmark.excerpt).assertIsDisplayed()
    }
}
