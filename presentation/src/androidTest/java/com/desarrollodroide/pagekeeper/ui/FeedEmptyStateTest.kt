package com.desarrollodroide.pagekeeper.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.ui.feed.FeedActions
import com.desarrollodroide.pagekeeper.ui.feed.FeedView
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The feed used to greet every cold start with "No bookmarks yet" and a Refresh button.
 *
 * The check was `itemCount > 0`, and on a cold start the count is 0 while the first load is still
 * running, so the empty state rendered for as long as Room took to answer. It is only empty once
 * the load has finished and still produced nothing.
 */
@RunWith(AndroidJUnit4::class)
class FeedEmptyStateTest {

    @get:Rule
    val rule = createComposeRule()

    private val noopActions = FeedActions(
        goToLogin = {},
        onBookmarkSelect = {},
        onRefreshFeed = {},
        onEditBookmark = {},
        onDeleteBookmark = {},
        onShareBookmark = {},
        onBookmarkEpub = {},
        onClickSync = {},
        onClearError = {},
        onCategoriesSelectedChanged = {},
    )

    /** Never completes, so the pager stays in its initial Loading state. */
    private class HangingSource(private val gate: CompletableDeferred<Unit>) :
        PagingSource<Int, Bookmark>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bookmark> {
            gate.await()
            return LoadResult.Page(emptyList(), null, null)
        }

        override fun getRefreshKey(state: PagingState<Int, Bookmark>) = null
    }

    private class EmptySource : PagingSource<Int, Bookmark>() {
        override suspend fun load(params: LoadParams<Int>) =
            LoadResult.Page<Int, Bookmark>(emptyList(), null, null)

        override fun getRefreshKey(state: PagingState<Int, Bookmark>) = null
    }

    @Composable
    private fun Feed(source: () -> PagingSource<Int, Bookmark>) {
        val items = Pager(PagingConfig(pageSize = 10)) { source() }.flow.collectAsLazyPagingItems()
        ShioriTheme {
            FeedView(
                actions = noopActions,
                viewType = BookmarkViewType.FULL,
                serverURL = "",
                xSessionId = "",
                token = "",
                bookmarksPagingItems = items,
                tagToHide = null,
                showOnlyHiddenTag = false,
            )
        }
    }

    @Test
    fun theEmptyStateDoesNotShowWhileTheFirstPageIsStillLoading() {
        val gate = CompletableDeferred<Unit>()
        rule.setContent { Feed { HangingSource(gate) } }

        rule.onNodeWithText("No bookmarks yet").assertDoesNotExist()
        rule.onNodeWithText("Refresh").assertDoesNotExist()
    }

    @Test
    fun theEmptyStateShowsOnceTheLoadFinishedWithNothingInIt() {
        rule.setContent { Feed { EmptySource() } }

        rule.onNodeWithText("No bookmarks yet").assertIsDisplayed()
        rule.onNodeWithText("Refresh").assertIsDisplayed()
    }
}
