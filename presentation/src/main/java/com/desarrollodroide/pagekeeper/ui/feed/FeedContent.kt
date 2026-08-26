package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeedContent(
    actions: FeedActions,
    viewType: BookmarkViewType,
    serverURL: String,
    xSessionId: String,
    token: String,
    bookmarksPagingItems: LazyPagingItems<Bookmark>,
    tagToHide: Tag?,
    showOnlyHiddenTag: Boolean,
    selectedIds: Set<Int> = emptySet(),
    refreshingIds: Set<Int> = emptySet(),
    settledIds: Set<Int> = emptySet(),
) {
    val refreshCoroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val gridState = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(bookmarksPagingItems.loadState.refresh) {
        if (bookmarksPagingItems.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            gridState.animateScrollToItem(0)
            delay(100)
            isRefreshing = false
        }
    }

    // Scroll to top when a new bookmark is added (item count increases)
    var previousItemCount by remember { mutableIntStateOf(bookmarksPagingItems.itemCount) }
    LaunchedEffect(bookmarksPagingItems.itemCount) {
        if (bookmarksPagingItems.itemCount > previousItemCount && previousItemCount > 0) {
            gridState.animateScrollToItem(0)
        }
        previousItemCount = bookmarksPagingItems.itemCount
    }

    fun refreshBookmarks() {
        refreshCoroutineScope.launch {
            actions.onRefreshFeed.invoke()
            isRefreshing = true
            delay(1500)
            isRefreshing = false
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // PullToRefreshBox is the M3 pull-to-refresh; it replaces the copy of the old Material 2
    // implementation that used to live under ui/components/pulltorefresh. The indicator is the
    // Expressive shape-morphing loading indicator rather than the legacy arrow spinner.
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = ::refreshBookmarks,
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        // Adaptive columns rather than a fixed single column. A phone in portrait still gets
        // one, but a landscape phone, a tablet or an unfolded foldable gets two or three instead
        // of one card stretched across the whole width, which was unreadable and let a single
        // item fill the entire viewport.
        //
        // Staggered, not a plain grid. Cards carry a variable amount of text, and a plain grid
        // makes a line as tall as its tallest card while giving the others no way to fill it:
        // fillMaxHeight on an item is a no op there, because the line measures its children with
        // an unbounded height. So the short card stopped early and left its neighbour's edge
        // hanging. The alternatives were padding every card out to a common height, which cost
        // 130dp of blank on a bookmark with no excerpt and no tags. Here each card is as tall as
        // its own content and the columns simply pack, so there is no line to be level with.
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                count = bookmarksPagingItems.itemCount,
                key = { index ->
                    // Including 'modified' in the key ensures that when a bookmark's 'modified' field changes,
                    // Compose recognizes it as a new item and recomposes it. This updates the UI immediately
                    // after data changes
                    val bookmark = bookmarksPagingItems[index]
                    if (bookmark != null) "${bookmark.id}_${bookmark.modified}" else "index_$index"
                }
            ) { index ->
                val bookmark = bookmarksPagingItems[index]
                if (bookmark != null) {
                    BookmarkItem(
                        getBookmark = { bookmark },
                        serverURL = serverURL,
                        xSessionId = xSessionId,
                        token = token,
                        viewType = viewType,
                        isSelected = bookmark.id in selectedIds,
                        isSelectionMode = selectedIds.isNotEmpty(),
                        isRefreshing = bookmark.id in refreshingIds,
                        hasSettledEmpty = bookmark.id in settledIds,
                        actions = BookmarkActions(
                            onClickEdit = { getBookmark -> actions.onEditBookmark(getBookmark()) },
                            onClickDelete = { getBookmark -> actions.onDeleteBookmark(getBookmark()) },
                            onClickShare = { getBookmark -> actions.onShareBookmark(getBookmark()) },
                            onClickBookmark = { getBookmark -> actions.onBookmarkSelect(getBookmark()) },
                            onClickEpub = { getBookmark -> actions.onBookmarkEpub(getBookmark()) },
                            onClickSync = { getBookmark -> actions.onClickSync(getBookmark()) },
                            onClickRefresh = { getBookmark -> actions.onRefreshBookmark(getBookmark()) },
                            onClickCategory = { },
                            onToggleSelection = { actions.onToggleSelection(bookmark) },
                        ),
                    )
                }
            }
            bookmarksPagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item(span = StaggeredGridItemSpan.FullLine) { PageLoader(modifier = Modifier.fillMaxWidth()) }
                    }

                    loadState.refresh is LoadState.Error -> {
                        val error = loadState.refresh as LoadState.Error
                        if (error.error.localizedMessage == SESSION_HAS_BEEN_EXPIRED) {
                            actions.goToLogin()
                        } else {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                ErrorMessage(
                                    modifier = Modifier.fillMaxWidth(),
                                    message = error.error.localizedMessage ?: "Unknown error",
                                    onClickRetry = { retry() })
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item(span = StaggeredGridItemSpan.FullLine) { LoadingNextPageItem() }
                    }

                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item(span = StaggeredGridItemSpan.FullLine) {
                            ErrorMessage(
                                message = error.error.localizedMessage ?: "Unknown error",
                                onClickRetry = { retry() })
                        }
                    }
                }
            }
            item(span = StaggeredGridItemSpan.FullLine) { Spacer(modifier = Modifier.height(88.dp)) }
        }

        // Whether the last movement was towards the top of the list. Kept in state rather than
        // derived, because it is a fact about a gesture that has already happened: once the scroll
        // stops the value has to stay, or the button would vanish under the finger that was
        // reaching for it.
        var scrollingUp by remember { mutableStateOf(false) }
        LaunchedEffect(gridState) {
            var previousIndex = gridState.firstVisibleItemIndex
            var previousOffset = gridState.firstVisibleItemScrollOffset
            snapshotFlow {
                gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                scrollingUp = movedTowardsTop(index, offset, previousIndex, previousOffset)
                previousIndex = index
                previousOffset = offset
            }
        }

        // Being scrolled off the first item is only half of it. Shown on the way down as well, this
        // button rides the whole descent stacked under the add fab, and in the 340dp list pane of
        // the two pane layout the pair covers about a third of a card to offer something nobody
        // reading forwards has asked for. Wanting to go back up is a deliberate move, and it is
        // enough of a signal to put the button there.
        val showScrollToTopButton by remember {
            derivedStateOf { gridState.firstVisibleItemIndex > 0 && scrollingUp }
        }

        // Bottom end, above the scaffold's add fab. Without the offset it sat underneath that fab
        // at identical coordinates: composed, invisible and impossible to tap.
        //
        // The add fab moves into the list pane along with the feed in two panes, so this offset
        // is real clearance in both layouts and the two buttons read as one stack in the corner.
        // While the fab stayed behind in the scaffold, at the bottom of the window and therefore
        // over the article, this was 88dp of nothing and left the button hovering in the middle of
        // the list against a card's action row.
        //
        // A card's action row is right aligned, so this corner is also where a card's delete icon
        // passes as the feed scrolls, and the two end up a few pixels apart. zIndex keeps this
        // button above the grid for hit testing as well as drawing, so a tap on it cannot reach
        // the card underneath.
        AnimatedVisibility(
            visible = showScrollToTopButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(1f)
                .padding(end = 16.dp, bottom = 88.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            // Same shape and colour as the add fab below it, one size down. As a muted square
            // next to a solid circle it read as something that had landed on the card behind it
            // rather than as the other half of a pair.
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch { gridState.animateScrollToItem(0) }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }
}

/**
 * Whether a grid moved towards the top of the list between two readings of its scroll position.
 *
 * The index alone cannot answer it: most scrolling happens inside one item, where the index does
 * not change at all and only the offset into it does. And the offset alone cannot answer it
 * either, because it resets to near zero every time the index changes, so a fall in the offset
 * across an item boundary is not movement upwards.
 *
 * Standing still counts as not moving up. The caller keeps the previous answer until the grid
 * moves again, so the button does not disappear the moment a scroll ends.
 */
internal fun movedTowardsTop(
    index: Int,
    offset: Int,
    previousIndex: Int,
    previousOffset: Int,
): Boolean = if (index != previousIndex) index < previousIndex else offset < previousOffset
