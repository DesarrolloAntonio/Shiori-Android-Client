package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import org.koin.androidx.compose.get

@Composable
fun BookmarkEditorScreen(
    pageTitle: String,
    bookmarkEditorType: BookmarkEditorType,
    bookmark: Bookmark,
    onBack: () -> Unit,
    updateBookmark: (Bookmark) -> Unit,
    showToast: (String) -> Unit = {},
    startMainActivity: () -> Unit = {}
) {
    val bookmarkViewModel = get<BookmarkViewModel>()
    val newTag = remember { mutableStateOf("") }
    val availableTags = bookmarkViewModel.availableTags.collectAsState()
    val bookmarkUiState = bookmarkViewModel.bookmarkUiState.collectAsState().value
    var currentUrl by remember { mutableStateOf(bookmark.url) }

    // No need to update values in settings
    var localCreateEbook by remember { mutableStateOf(bookmarkViewModel.createEbook) }
    var localCreateArchive by remember { mutableStateOf(bookmarkViewModel.createArchive) }
    val assignedTags: MutableState<List<Tag>> = remember { mutableStateOf(bookmark.tags) }
        var localMakeArchivePublic by remember {
        mutableStateOf(
            when (bookmarkEditorType) {
                BookmarkEditorType.ADD, BookmarkEditorType.ADD_MANUALLY -> bookmarkViewModel.makeArchivePublic
                BookmarkEditorType.EDIT -> bookmark.public == 1
            }
        )
    }
    BackHandler {
        onBack()
    }
    if (bookmarkUiState.isLoading) {
        Log.v("BookmarkEditorScreen", "isLoading")
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!bookmarkUiState.error.isNullOrEmpty()) {
        Log.v("BookmarkEditorScreen", "Error")
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = bookmarkUiState.error,
            dismissButton = if (bookmarkViewModel.sessionExpired) "Go to login" else "",
            confirmButton = "Accept",
            openDialog = remember { mutableStateOf(true) },
            onDismiss = {
                if (bookmarkViewModel.sessionExpired) {
                    startMainActivity()
                }
            },
            onConfirm = {}
        )
    }

    BookmarkEditorView(
        title = pageTitle,
        url = currentUrl,
        bookmarkEditorType = bookmarkEditorType,
        newTag = newTag,
        assignedTags = assignedTags,
        availableTags = availableTags,
        saveBookmark = {
            when (bookmarkEditorType) {
                BookmarkEditorType.ADD, BookmarkEditorType.ADD_MANUALLY -> {
                    bookmarkViewModel.saveBookmark(
                        url = currentUrl,
                        title = bookmark.title,
                        tags = assignedTags.value,
                        createArchive = localCreateArchive,
                        makeArchivePublic = localMakeArchivePublic,
                        createEbook = localCreateEbook
                    )
                }
                BookmarkEditorType.EDIT -> {
                    bookmarkViewModel.editBookmark(
                        bookmark = bookmark.copy(
                            tags = assignedTags.value,
                            createEbook = bookmark.hasEbook,
                            createArchive = bookmark.hasArchive,
                            public = if (localMakeArchivePublic) 1 else 0,
                        )
                    )
                }
            }
            onBack()
        },
        onBackClick = onBack,
        createArchive = localCreateArchive,
        makeArchivePublic = localMakeArchivePublic,
        onMakeArchivePublicChanged = { localMakeArchivePublic = it },
        createEbook = localCreateEbook,
        onCreateEbookChanged = { localCreateEbook = it },
        onCreateArchiveChanged = { localCreateArchive = it },
        onUrlChange = { currentUrl = it }
    )
}


