package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.desarrollodroide.pagekeeper.extensions.shareText
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorScreen
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorType
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser

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
    val bookmarkSelected: MutableState<Bookmark?> = remember { mutableStateOf(null) }
    val showDeleteConfirmationDialog = remember { mutableStateOf(false) }
    val bookmarkToDelete: MutableState<Bookmark?> = remember { mutableStateOf(null) }

    FeedContent(
        bookmarksUiState = feedViewModel.bookmarksUiState.collectAsState().value,
        goToLogin = {
            feedViewModel.resetData()
            goToLogin.invoke()
        },
        onBookmarkSelect = {
            Log.v("FeedContent", feedViewModel.getUrl(it))
            openUrlInBrowser.invoke(feedViewModel.getUrl(it))
        },
        serverURL = feedViewModel.getServerUrl(),
        onRefreshFeed = {
            feedViewModel.refreshFeed()
        },
        onEditBookmark = { bookmark ->
            bookmarkSelected.value = bookmark
            showBookmarkEditorScreen.value = true
        },
        onDeleteBookmark = {
            bookmarkToDelete.value = it
            showDeleteConfirmationDialog.value = true
        },
        onShareBookmark = {
            context.shareText(it.url)
        },
        onClearError = {
            feedViewModel.resetData()
        },
        onBookmarkEpub = {
            openUrlInBrowser.invoke(feedViewModel.getEpubUrl(it))
        }
    )
    if (showBookmarkEditorScreen.value && bookmarkSelected.value != null) {
        Box(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { }
            }
        ) {
            bookmarkSelected.value?.let {
                BookmarkEditorScreen(
                    title = "Edit",
                    bookmarkEditorType = BookmarkEditorType.EDIT,
                    bookmark = it,
                    onBackClick = {
                        showBookmarkEditorScreen.value = false
                    },
                    updateBookmark = { bookMark ->
                        showBookmarkEditorScreen.value = false
                        feedViewModel.refreshFeed()
                    }
                )
            }
        }
    }
    if (showDeleteConfirmationDialog.value && bookmarkToDelete.value != null) {
        ConfirmDialog(
            title = "Confirmation",
            content = "Are you sure you want to delete this bookmark?",
            confirmButton = "Delete",
            dismissButton = "Cancel",
            onConfirm = {
                bookmarkToDelete.value?.let {
                    feedViewModel.deleteBookmark(it)
                    showDeleteConfirmationDialog.value = false
                }
            },
            openDialog = showDeleteConfirmationDialog,
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            )
        )
    }
}

@Composable
private fun FeedContent(
    goToLogin: () -> Unit,
    onBookmarkSelect: (Bookmark) -> Unit,
    onRefreshFeed: () -> Unit,
    onEditBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onBookmarkEpub: (Bookmark) -> Unit,
    onShareBookmark: (Bookmark) -> Unit,
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
        Log.v("bookmarksUiState", "Error")
    } else
        if (bookmarksUiState.data != null) {
            Log.v("bookmarksUiState", "Success")
            if (bookmarksUiState.data.isNotEmpty()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .nestedScroll(rememberNestedScrollInteropConnection()),
                    ) {
                        val uniqueCategories = remember { mutableStateOf(bookmarksUiState.data.flatMap { it.tags }.distinct()) }
                        DockedSearchBarWithCategories(
                            onBookmarkSelect = {
                                onBookmarkSelect.invoke(it)
                            },
                            bookmarks = bookmarksUiState.data.reversed(),
                            uniqueCategories = uniqueCategories,
                            serverURL = serverURL,
                            onRefreshFeed = onRefreshFeed,
                            onDeleteBookmark = onDeleteBookmark,
                            onEditBookmark = onEditBookmark,
                            onShareBookmark = onShareBookmark,
                            onBookmarkEpub = onBookmarkEpub
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
                        onRefresh = onRefreshFeed
                    )
                }
            }
        }
}

