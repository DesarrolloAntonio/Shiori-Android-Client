package com.desarrollodroide.pagekeeper.ui.feed

import android.media.MediaScannerConnection
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
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.pagekeeper.extensions.shareText
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorScreen
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorType
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.EpubOptionsDialog
import com.desarrollodroide.pagekeeper.ui.components.UpdateCacheDialog
import java.io.File

@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
    shareEpubFile: (File) -> Unit,
    isCategoriesVisible: Boolean,
    isSearchBarVisible: Boolean,
    setShowTopBar: (Boolean) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        feedViewModel.loadInitialData()
        feedViewModel.getBookmarks()
    }
    val actions = FeedActions(
        goToLogin = {
            feedViewModel.resetData()
            goToLogin()
        },
        onBookmarkSelect = { bookmark ->
            Log.v("FeedContent", feedViewModel.getUrl(bookmark))
            openUrlInBrowser(feedViewModel.getUrl(bookmark))
        },
        onRefreshFeed = {
            feedViewModel.refreshFeed()
        },
        onEditBookmark = { bookmark ->
            feedViewModel.bookmarkSelected.value = bookmark
            feedViewModel.showBookmarkEditorScreen.value = true
        },
        onDeleteBookmark = { bookmark ->
            feedViewModel.bookmarkToDelete.value = bookmark
            feedViewModel.showDeleteConfirmationDialog.value = true
        },
        onShareBookmark = { bookmark ->
            context.shareText(bookmark.url)
        },
        onBookmarkEpub = { bookmark ->
            feedViewModel.downloadFile(bookmark)
        },
        onClickSync = { bookmark ->
            feedViewModel.bookmarkToUpdateCache.value = bookmark
            feedViewModel.showSyncDialog.value = true
        },
        onClearError = {
            feedViewModel.resetData()
        },
        onCategoriesSelectedChanged = { categories ->
            feedViewModel.saveSelectedCategories(categories)
        }
    )
    FeedContent(
        actions = actions,
        bookmarksUiState = feedViewModel.bookmarksUiState.collectAsState().value,
        downloadUiState = feedViewModel.downloadUiState.collectAsState().value,
        selectedTags = feedViewModel.selectedCategories,
        serverURL = feedViewModel.getServerUrl(),
        xSessionId = feedViewModel.getSession(),
        shareEpubFile = shareEpubFile,
        isLegacyApi = feedViewModel.isLegacyApi(),
        token = feedViewModel.getToken(),
        viewType = feedViewModel.isCompactView.collectAsState().value.let { isCompactView ->
            if (isCompactView) BookmarkViewType.SMALL else BookmarkViewType.FULL
        },
        isCategoriesVisible = isCategoriesVisible,
        isSearchBarVisible = isSearchBarVisible,
        showEpubOptionsDialog = feedViewModel.showEpubOptionsDialog,
        uniqueCategories = feedViewModel.uniqueCategories,
        )
    if (feedViewModel.showBookmarkEditorScreen.value && feedViewModel.bookmarkSelected.value != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
        ) {
            feedViewModel.bookmarkSelected.value?.let {
                setShowTopBar(false)
                BookmarkEditorScreen(
                    title = "Edit",
                    bookmarkEditorType = BookmarkEditorType.EDIT,
                    bookmark = it,
                    onBackClick = {
                        setShowTopBar(true)
                        feedViewModel.showBookmarkEditorScreen.value = false
                    },
                    updateBookmark = { bookMark ->
                        setShowTopBar(true)
                        feedViewModel.showBookmarkEditorScreen.value = false
                        feedViewModel.refreshFeed()
                    }
                )
            }
        }
    }
    if (feedViewModel.showDeleteConfirmationDialog.value && feedViewModel.bookmarkToDelete.value != null) {
        ConfirmDialog(
            title = "Confirmation",
            content = "Are you sure you want to delete this bookmark?",
            confirmButton = "Delete",
            dismissButton = "Cancel",
            onConfirm = {
                feedViewModel.bookmarkToDelete.value?.let {
                    feedViewModel.deleteBookmark(it)
                    feedViewModel.showDeleteConfirmationDialog.value = false
                }
            },
            openDialog = feedViewModel.showDeleteConfirmationDialog,
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            )
        )
    }
    UpdateCacheDialog(
        showDialog = feedViewModel.showSyncDialog,
        onConfirm = { keepOldTitle, updateArchive, updateEbook ->
            feedViewModel.updateBookmark(
                keepOldTitle = keepOldTitle,
                updateEbook = updateEbook,
                updateArchive = updateArchive,
            )
        }
    )
}

@Composable
private fun FeedContent(
    actions: FeedActions,
    viewType: BookmarkViewType,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    bookmarksUiState: UiState<List<Bookmark>>,
    downloadUiState: UiState<File>,
    shareEpubFile: (File) -> Unit,
    isCategoriesVisible: Boolean,
    isSearchBarVisible: Boolean,
    showEpubOptionsDialog: MutableState<Boolean>,
    selectedTags: MutableState<List<Tag>>,
    uniqueCategories: MutableState<List<Tag>>,
    ) {
    if (bookmarksUiState.isLoading || downloadUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!bookmarksUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = bookmarksUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                actions.onClearError()
                actions.goToLogin.invoke()
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
                        DockedSearchBarWithCategories(
                            actions = actions,
                            bookmarks = bookmarksUiState.data.reversed(),
                            uniqueCategories = uniqueCategories,
                            serverURL = serverURL,
                            xSessionId = xSessionId,
                            isLegacyApi = isLegacyApi,
                            token = token,
                            viewType = viewType,
                            isCategoriesVisible = isCategoriesVisible,
                            isSearchBarVisible = isSearchBarVisible,
                            selectedTags = selectedTags,
                        )
                    }
                }
            } else {
                EmptyView(actions)
            }
        } else {
            EmptyView(actions)
        }

    if (!downloadUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Download Error",
            content = downloadUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = { },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            ),
        )
    }

    if (downloadUiState.data != null && showEpubOptionsDialog.value) {
        val context = LocalContext.current
        MediaScannerConnection.scanFile(context, arrayOf(downloadUiState.data.absolutePath),null) { path, uri -> }
        EpubOptionsDialog(
            icon = Icons.Default.Error,
            title = "Success",
            content = "Epub file downloaded, would you like to share it?",
            onClickOption = { index ->
                when (index) {
                    2 -> { shareEpubFile.invoke(downloadUiState.data) }
                }
            },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            ),
            showDialog = showEpubOptionsDialog
        )
    }
}

@Composable
private fun EmptyView(actions: FeedActions) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NoContentView(
            modifier = Modifier
                .padding(top = 100.dp)
                .align(Alignment.Center),
            onRefresh = actions.onRefreshFeed
        )
    }
}

data class FeedActions(
    val goToLogin: () -> Unit,
    val onBookmarkSelect: (Bookmark) -> Unit,
    val onRefreshFeed: () -> Unit,
    val onEditBookmark: (Bookmark) -> Unit,
    val onDeleteBookmark: (Bookmark) -> Unit,
    val onShareBookmark: (Bookmark) -> Unit,
    val onBookmarkEpub: (Bookmark) -> Unit,
    val onClickSync: (Bookmark) -> Unit,
    val onClearError: () -> Unit,
    val onCategoriesSelectedChanged: (List<Tag>) -> Unit
)
