package com.shiori.androidclient.ui.feed

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shiori.androidclient.extensions.openInBrowser
import com.shiori.androidclient.extensions.openUrlInBrowser
import com.shiori.androidclient.extensions.shareText
import com.shiori.androidclient.ui.bookmarkeditor.BookmarkEditorScreen
import com.shiori.androidclient.ui.bookmarkeditor.BookmarkEditorType
import com.shiori.androidclient.ui.components.ConfirmDialog
import com.shiori.androidclient.ui.components.InfiniteProgressDialog
import com.shiori.androidclient.ui.components.UiState
import com.shiori.model.Bookmark
import com.shiori.model.Tag

@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    openUrlInBrowser: (String) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        feedViewModel.refreshUrl()
        feedViewModel.getBookmarks()
    }
    val showBookmarkEditorScreen = remember { mutableStateOf(false)}
    val uniqueCategories = remember { mutableStateOf(emptyList<Tag>()) }
    val bookmarkSelected: MutableState<Bookmark?> = remember { mutableStateOf(null) }

    FeedContent(
        bookmarksUiState = feedViewModel.bookmarksUiState.collectAsState().value,
        goToLogin = {
            feedViewModel.resetData()
            goToLogin.invoke()
        },
        onBookmarkClick = {
            Log.v("FeedContent", feedViewModel.getUrl(it))
            openUrlInBrowser.invoke(feedViewModel.getUrl(it))
            //context.openUrlInBrowser(feedViewModel.getUrl(it))
            //feedViewModel.getUrl(it).openInBrowser(context)
        },
        serverURL = feedViewModel.getServerUrl(),
        onPullToRefresh = {
            feedViewModel.refreshFeed()
        },
        onClickEdit = { bookmark ->
            bookmarkSelected.value = bookmark
            uniqueCategories.value = bookmark.tags
            showBookmarkEditorScreen.value = true
        },
        onclickDelete = {
            feedViewModel.deleteBookmark(it)
        },
        onClickShare = {
            context.shareText(it.url)
        },
        onClearError = {
            feedViewModel.resetData()
        }
    )
    if (showBookmarkEditorScreen.value && bookmarkSelected.value != null) {
        bookmarkSelected.value?.let {
            BookmarkEditorScreen(
                title  = "Edit",
                bookmarkEditorType = BookmarkEditorType.EDIT,
                bookmark = it,
                onBackClick = {
                    showBookmarkEditorScreen.value = false
                },
                updateBookmark = { bookMark ->
                    showBookmarkEditorScreen.value = false
                    feedViewModel.updateBookmark(bookMark)
                }
            )
        }
    }
}

@Composable
private fun FeedContent(
    goToLogin: () -> Unit,
    onBookmarkClick: (Bookmark) -> Unit,
    onPullToRefresh: () -> Unit,
    onClickEdit: (Bookmark) -> Unit,
    onclickDelete: (Bookmark) -> Unit,
    onClickShare: (Bookmark) -> Unit,
    onClearError: () -> Unit,
    serverURL: String,
    bookmarksUiState: UiState<List<Bookmark>>
) {
    if (bookmarksUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!bookmarksUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = bookmarksUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                onClearError()
                goToLogin.invoke()
            },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            ),
        )
        Log.v("loginUiState", "Error")
    } else
        if (bookmarksUiState.data != null) {
            Log.v("loginUiState", "Success")
            if (bookmarksUiState.data.isNotEmpty()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .nestedScroll(rememberNestedScrollInteropConnection()),
                    ) {
                        DockedSearchBarWithCategories(
                            onBookmarkClick = {
                                onBookmarkClick.invoke(it)
                            },
                            bookmarks = bookmarksUiState.data,
                            serverURL = serverURL,
                            onRefresh = onPullToRefresh,
                            onDeleteClick = onclickDelete,
                            onEditClick = onClickEdit,
                            onShareClick = onClickShare
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NoContentView(
                        modifier = Modifier
                            .padding(top = 100.dp)
                            .align(Alignment.Center),
                        onRefresh = onPullToRefresh
                    )
                }
            }
        }
}

