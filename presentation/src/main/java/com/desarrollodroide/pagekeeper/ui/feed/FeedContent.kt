package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.pagekeeper.ui.components.pulltorefresh.PullRefreshIndicator
import com.desarrollodroide.pagekeeper.ui.components.pulltorefresh.pullRefresh
import com.desarrollodroide.pagekeeper.ui.components.pulltorefresh.rememberPullRefreshState
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedContent(
    actions: FeedActions,
    viewType: BookmarkViewType,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    bookmarksPagingItems: LazyPagingItems<Bookmark>,
    tagToHide: Tag?,
    showOnlyHiddenTag: Boolean
) {
    val refreshCoroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    fun refreshBookmarks() = refreshCoroutineScope.launch {
        actions.onRefreshFeed.invoke()
        isRefreshing = true
        delay(1500)
        isRefreshing = false
    }

    val refreshState = rememberPullRefreshState(isRefreshing, ::refreshBookmarks)
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxHeight()) {
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp)
                .pullRefresh(state = refreshState)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                count = bookmarksPagingItems.itemCount,
                key = { index ->
                    bookmarksPagingItems[index]?.id ?: index
                }
            ) { index ->
                val bookmark = bookmarksPagingItems[index]
                val shouldShowBookmark = when {
                    tagToHide == null -> true
                    showOnlyHiddenTag -> bookmark?.tags?.any { it.id == tagToHide.id } == true
                    else -> bookmark?.tags?.none { it.id == tagToHide.id } == true
                }
                if (bookmark != null && shouldShowBookmark) {

                    BookmarkItem(
                        getBookmark = { bookmark },
                        serverURL = serverURL,
                        xSessionId = xSessionId,
                        token = token,
                        isLegacyApi = isLegacyApi,
                        viewType = viewType,
                        actions = BookmarkActions(
                            onClickEdit = { getBookmark -> actions.onEditBookmark(getBookmark()) },
                            onClickDelete = { getBookmark -> actions.onDeleteBookmark(getBookmark()) },
                            onClickShare = { getBookmark -> actions.onShareBookmark(getBookmark()) },
                            onClickBookmark = { getBookmark -> actions.onBookmarkSelect(getBookmark()) },
                            onClickEpub = { getBookmark -> actions.onBookmarkEpub(getBookmark()) },
                            onClickSync = { getBookmark -> actions.onClickSync(getBookmark()) },
                            onClickCategory = { category -> }
                        ),
                    )
                    if (index < bookmarksPagingItems.itemCount - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .height(1.dp)
                                .padding(horizontal = 6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }
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
                        item { LoadingNextPageItem(modifier = Modifier) }
                    }
                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item {
                            ErrorMessage(
                                modifier = Modifier,
                                message = error.error.localizedMessage ?: "Unknown error",
                                onClickRetry = { retry() })
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.padding(4.dp)) }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(alignment = Alignment.TopCenter),
            refreshing = isRefreshing,
            state = refreshState,
        )
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
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
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
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .clickable {
                            onClickSuggestion(bookmark)
                        }
                        .background(Color.Transparent),
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
