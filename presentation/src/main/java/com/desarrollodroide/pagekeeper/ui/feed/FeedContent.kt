package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
) {
    val refreshCoroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
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
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        actions = BookmarkActions(
                            onClickEdit = { getBookmark -> actions.onEditBookmark(getBookmark()) },
                            onClickDelete = { getBookmark -> actions.onDeleteBookmark(getBookmark()) },
                            onClickShare = { getBookmark -> actions.onShareBookmark(getBookmark()) },
                            onClickBookmark = { getBookmark -> actions.onBookmarkSelect(getBookmark()) },
                            onClickEpub = { getBookmark -> actions.onBookmarkEpub(getBookmark()) },
                            onClickSync = { getBookmark -> actions.onClickSync(getBookmark()) },
                            onClickCategory = { },
                            onToggleSelection = { actions.onToggleSelection(bookmark) },
                        ),
                    )
                }
            }
            bookmarksPagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) { PageLoader(modifier = Modifier.fillMaxWidth()) }
                    }

                    loadState.refresh is LoadState.Error -> {
                        val error = loadState.refresh as LoadState.Error
                        if (error.error.localizedMessage == SESSION_HAS_BEEN_EXPIRED) {
                            actions.goToLogin()
                        } else {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorMessage(
                                    modifier = Modifier.fillMaxWidth(),
                                    message = error.error.localizedMessage ?: "Unknown error",
                                    onClickRetry = { retry() })
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) { LoadingNextPageItem() }
                    }

                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorMessage(
                                message = error.error.localizedMessage ?: "Unknown error",
                                onClickRetry = { retry() })
                        }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(88.dp)) }
        }

        val showScrollToTopButton by remember {
            derivedStateOf { gridState.firstVisibleItemIndex > 0 }
        }

        // Offset above the scaffold's add fab, which owns the bottom end corner. Without the
        // offset this sat underneath it at identical coordinates: composed, invisible and
        // impossible to tap. Small, so the primary action stays the prominent one.
        AnimatedVisibility(
            visible = showScrollToTopButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch { gridState.animateScrollToItem(0) }
                },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }
}
