package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.pagekeeper.ui.components.Categories
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
fun DockedSearchBarWithCategories(
    actions: FeedActions,
    bookmarks: List<Bookmark>,
    viewType: BookmarkViewType,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    uniqueCategories: MutableState<List<Tag>>,
    isCategoriesVisible: Boolean,
    isSearchBarVisible: Boolean,
    selectedTags: MutableState<List<Tag>>,
) {
    val searchTextState = rememberSaveable { mutableStateOf("") }
    val isActive = rememberSaveable { mutableStateOf(false) }

    val filteredBookmarks = if (selectedTags.value.isEmpty()) {
        bookmarks
    } else {
        bookmarks.filter { bookmark ->
            bookmark.tags.any { it in selectedTags.value }
        }
    }
    val refreshCoroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    fun refreshBookmarks() = refreshCoroutineScope.launch {
        actions.onRefreshFeed.invoke()
        isRefreshing = true
        delay(1500)
        isRefreshing = false
    }

    val refreshState = rememberPullRefreshState(isRefreshing, ::refreshBookmarks)

    Box(Modifier.fillMaxHeight()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp)
                .pullRefresh(state = refreshState)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                AnimatedVisibility(isSearchBarVisible) {
                    SearchBarWithFilters(
                        searchText = searchTextState,
                        isActive = isActive,
                        bookmarks = bookmarks,
                        onBookmarkClick = actions.onBookmarkSelect,
                    )
                }
            }
            item {
                Categories(
                    showCategories = isCategoriesVisible,
                    uniqueCategories = uniqueCategories,
                    selectedTags = selectedTags,
                    onCategoriesSelectedChanged = actions.onCategoriesSelectedChanged
                )
            }
            items(filteredBookmarks) {
                BookmarkItem(
                    bookmark = it,
                    serverURL = serverURL,
                    xSessionId = xSessionId,
                    token = token,
                    isLegacyApi = isLegacyApi,
                    viewType = viewType,
                    actions = BookmarkActions(
                        onClickEdit = { actions.onEditBookmark(it) },
                        onClickDelete = { actions.onDeleteBookmark(it) },
                        onClickShare = { actions.onShareBookmark(it) },
                        onClickBookmark = { actions.onBookmarkSelect(it) },
                        onClickEpub = { actions.onBookmarkEpub(it) },
                        onClickSync = { actions.onClickSync(it) },
                        onClickCategory = { category ->
                            it.tags.firstOrNull() { it.name == category.name }?.apply {
                                if (selectedTags.value.contains(category)) {
                                    selectedTags.value = selectedTags.value - category
                                } else {
                                    selectedTags.value = selectedTags.value + category
                                }
                            }
                        }),
                )
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(alignment = Alignment.TopCenter),
            refreshing = isRefreshing,
            state = refreshState,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchBarWithFilters(
    searchText: MutableState<String>,
    isActive: MutableState<Boolean>,
    onBookmarkClick: (Bookmark) -> Unit,
    bookmarks: List<Bookmark>,
) {
    val filteredBookmarks =
        bookmarks.filter { it.title.contains(searchText.value, ignoreCase = true) }
    Box(Modifier
        .semantics { isContainer = true }
        .zIndex(1f)
        .fillMaxWidth()) {
        androidx.compose.material3.DockedSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            query = searchText.value,
            onQueryChange = { searchText.value = it },
            onSearch = { isActive.value = false },
            active = isActive.value,
            onActiveChange = { isActive.value = it },
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                Row() {
                    Box(modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            searchText.value = ""
                        }) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                    }
                }
            },
        ) {
            BookmarkSuggestions(
                bookmarks = filteredBookmarks,
                isActive = isActive,
                onClickSuggestion = onBookmarkClick
            )
        }
    }
}

@Composable
private fun BookmarkSuggestions(
    bookmarks: List<Bookmark>,
    isActive: MutableState<Boolean>,
    onClickSuggestion: (Bookmark) -> Unit
) {
    LazyColumn(
        modifier = Modifier.wrapContentHeight(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(bookmarks) { bookmark ->
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .clickable {
                        onClickSuggestion(bookmark)
                        isActive.value = false
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
                leadingContent = { Icon(Icons.Rounded.Star, contentDescription = null) },
            )
        }
    }
}

