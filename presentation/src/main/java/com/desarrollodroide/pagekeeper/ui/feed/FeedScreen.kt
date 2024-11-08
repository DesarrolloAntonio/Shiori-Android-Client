package com.desarrollodroide.pagekeeper.ui.feed

import android.media.MediaScannerConnection
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.pagekeeper.extensions.shareText
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorScreen
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorType
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.EpubOptionsDialog
import com.desarrollodroide.pagekeeper.ui.components.UpdateCacheDialog
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    goToReadableContent:(Bookmark) -> Unit,
    openUrlInBrowser: (String) -> Unit,
    shareEpubFile: (File) -> Unit,
    isCategoriesVisible: MutableState<Boolean>,
    isSearchBarVisible: MutableState<Boolean>,
    setShowTopBar: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val tagsState by feedViewModel.tagsState.collectAsState()
    val tagToHide by feedViewModel.tagToHide.collectAsState()
    val showOnlyHiddenTag by feedViewModel.showOnlyHiddenTag.collectAsState()

    LaunchedEffect(feedViewModel) {
        feedViewModel.initializeIfNeeded()
    }
    LaunchedEffect(isCategoriesVisible.value) {
        if (isCategoriesVisible.value) {
            // TODO remove when sync functionality is implemented
            //feedViewModel.getTags()
        }
    }

    val bookmarksPagingItems: LazyPagingItems<Bookmark> =
        feedViewModel.bookmarksState.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        snapshotFlow { bookmarksPagingItems.itemSnapshotList.items }
            .collect { updatedItems ->
                Log.d("FeedScreen", "Los bookmarks se han modificado: ${updatedItems.size} items")
            }
    }



    val bookmarksUiState = feedViewModel.bookmarksUiState.collectAsState().value
    val downloadUiState = feedViewModel.downloadUiState.collectAsState()
    val isCompactView by feedViewModel.compactView.collectAsState()


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val sheetStateCategories = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    val actions = FeedActions(
        goToLogin = {
            goToLogin()
        },
        onBookmarkSelect = { bookmark ->
            goToReadableContent(bookmark)
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
        onCategoriesSelectedChanged = { categories -> },
    )

    LaunchedEffect(bookmarksPagingItems.loadState) {
        val loadState = bookmarksPagingItems.loadState.refresh
        if (loadState is LoadState.Error) {
            val error = loadState.error
            if (error.message == SESSION_HAS_BEEN_EXPIRED) {
                feedViewModel.handleLoadState(loadState)
            }
        }
    }

    FeedView(
        actions = actions,
        serverURL = feedViewModel.getServerUrl(),
        xSessionId = feedViewModel.getSession(),
        token = feedViewModel.getToken(),
        viewType = if (isCompactView) BookmarkViewType.SMALL else BookmarkViewType.FULL,
        bookmarksPagingItems = bookmarksPagingItems,
        tagToHide = tagToHide,
        showOnlyHiddenTag = showOnlyHiddenTag
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
                    onBack = {
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
                    feedViewModel.deleteLocalBookmark(it)
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
    if (bookmarksUiState.isLoading || downloadUiState.value.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!bookmarksUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = bookmarksUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                if (bookmarksUiState.error == SESSION_HAS_BEEN_EXPIRED){
                    actions.onClearError()
                    actions.goToLogin.invoke()
                }
            },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            ),
        )
        Log.v("bookmarksUiState", "Error")
    }
    val isUpdating = feedViewModel.bookmarksUiState.collectAsState().value.isUpdating
    UpdateCacheDialog(
        isLoading = isUpdating,
        showDialog = feedViewModel.showSyncDialog
    ) { keepOldTitle, updateArchive, updateEbook ->
        feedViewModel.updateBookmark(
            keepOldTitle = keepOldTitle,
            updateEbook = updateEbook,
            updateArchive = updateArchive,
        )
        feedViewModel.showSyncDialog.value = false
    }
    if (!downloadUiState.value.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Download Error",
            content = downloadUiState.value.error?:"Unknown error",
            openDialog = remember { mutableStateOf(true) },
            onConfirm = { },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            ),
        )
    }

    if (downloadUiState.value.data != null && feedViewModel.showEpubOptionsDialog.value) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(downloadUiState.value.data?.absolutePath),
            null
        ) { path, uri -> }
        EpubOptionsDialog(
            icon = Icons.Default.Error,
            title = "Success",
            content = "Epub file downloaded, would you like to share it?",
            onClickOption = { index ->
                when (index) {
                    2 -> {
                        shareEpubFile.invoke(downloadUiState.value.data!!)
                    }
                }
            },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            ),
            showDialog = feedViewModel.showEpubOptionsDialog
        )
    }

    if (isSearchBarVisible.value) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            shape = BottomSheetDefaults.ExpandedShape,
            onDismissRequest = {
                isSearchBarVisible.value = false
            },
            sheetState = sheetState,
            dragHandle = null
        ) {
            SearchBar(
                onBookmarkClick =  actions.onBookmarkSelect,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        isSearchBarVisible.value = false
                    }
                }
            )
        }
    }

    if (isCategoriesVisible.value) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            shape = BottomSheetDefaults.ExpandedShape,
            onDismissRequest = {
                isCategoriesVisible.value = false
            },
            sheetState = sheetStateCategories,
        ) {
            val selectedTags by feedViewModel.selectedTags.collectAsState()
            CategoriesView(
                onDismiss = {
                    scope.launch {
                        sheetStateCategories.hide()
                        isCategoriesVisible.value = false
                    }
                },
                uniqueCategories = tagsState.data ?: emptyList(),
                tagToHide = tagToHide,
                onFilterHiddenTag = { value ->
                    feedViewModel.showOnlyHiddenTag.value = value
                },
                selectedOptionIndex = feedViewModel.selectedOptionIndex.value,
                onSelectedOptionIndexChanged = { newIndex ->
                    feedViewModel.selectedOptionIndex.value = newIndex
                },
                selectedTags = selectedTags,
                onCategoryDeselected = { tag ->
                    feedViewModel.removeSelectedTag(tag)
                },
                onCategorySelected = { tag ->
                    feedViewModel.addSelectedTag(tag)
                },
                onResetAll = {
                    feedViewModel.resetTags()
                },
            )
        }
    }
}

@Composable
private fun FeedView(
    actions: FeedActions,
    viewType: BookmarkViewType,
    serverURL: String,
    xSessionId: String,
    token: String,
    bookmarksPagingItems: LazyPagingItems<Bookmark>,
    tagToHide: Tag?,
    showOnlyHiddenTag: Boolean
) {
    if (bookmarksPagingItems.itemCount > 0) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(rememberNestedScrollInteropConnection()),
            ) {
                FeedContent(
                    actions = actions,
                    serverURL = serverURL,
                    xSessionId = xSessionId,
                    token = token,
                    viewType = viewType,
                    bookmarksPagingItems = bookmarksPagingItems,
                    tagToHide = tagToHide,
                    showOnlyHiddenTag = showOnlyHiddenTag
                )
            }
        }
    } else  {
        EmptyView(actions)
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
    val onCategoriesSelectedChanged: (List<Tag>) -> Unit,
)