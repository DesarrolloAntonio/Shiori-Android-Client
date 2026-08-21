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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
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
    showOnlyHiddenTag: Boolean
) {
    val refreshCoroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(bookmarksPagingItems.loadState.refresh) {
        if (bookmarksPagingItems.loadState.refresh is LoadState.NotLoading && isRefreshing) {
            listState.animateScrollToItem(0)
            delay(100)
            isRefreshing = false
        }
    }

    // Scroll to top when a new bookmark is added (item count increases)
    var previousItemCount by remember { mutableIntStateOf(bookmarksPagingItems.itemCount) }
    LaunchedEffect(bookmarksPagingItems.itemCount) {
        if (bookmarksPagingItems.itemCount > previousItemCount && previousItemCount > 0) {
            listState.animateScrollToItem(0)
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
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        actions = BookmarkActions(
                            onClickEdit = { getBookmark -> actions.onEditBookmark(getBookmark()) },
                            onClickDelete = { getBookmark -> actions.onDeleteBookmark(getBookmark()) },
                            onClickShare = { getBookmark -> actions.onShareBookmark(getBookmark()) },
                            onClickBookmark = { getBookmark -> actions.onBookmarkSelect(getBookmark()) },
                            onClickEpub = { getBookmark -> actions.onBookmarkEpub(getBookmark()) },
                            onClickSync = { getBookmark -> actions.onClickSync(getBookmark()) },
                            onClickCategory = { }
                        ),
                    )
                }
            }
            bookmarksPagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { PageLoader(modifier = Modifier.fillParentMaxSize()) }
                    }

                    loadState.refresh is LoadState.Error -> {
                        val error = loadState.refresh as LoadState.Error
                        if (error.error.localizedMessage == SESSION_HAS_BEEN_EXPIRED) {
                            actions.goToLogin()
                        } else {
                            item {
                                ErrorMessage(
                                    modifier = Modifier.fillParentMaxSize(),
                                    message = error.error.localizedMessage ?: "Unknown error",
                                    onClickRetry = { retry() })
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item { LoadingNextPageItem() }
                    }

                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item {
                            ErrorMessage(
                                message = error.error.localizedMessage ?: "Unknown error",
                                onClickRetry = { retry() })
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(88.dp)) }
        }

        val showScrollToTopButton by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 0 }
        }

        AnimatedVisibility(
            visible = showScrollToTopButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }
}

@Composable
fun BookmarkSuggestions(
    bookmarks: LazyPagingItems<Bookmark>,
    onClickSuggestion: (Bookmark) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(bookmarks.itemCount) { index ->
            val bookmark = bookmarks[index]
            if (bookmark != null) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onClickSuggestion(bookmark) },
                    headlineContent = {
                        Text(
                            text = bookmark.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = {
                        Text(
                            overflow = TextOverflow.Ellipsis,
                            text = bookmark.excerpt,
                            maxLines = 3,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingContent = { Icon(Icons.Rounded.Bookmark, contentDescription = null) },
                )
            }
        }
    }
}
