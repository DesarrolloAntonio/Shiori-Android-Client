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
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import org.koin.androidx.compose.get

@Composable
fun BookmarkEditorScreen(
    title: String,
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
    val makeArchivePublic by bookmarkViewModel.makeArchivePublic.collectAsState()

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

    val assignedTags: MutableState<List<Tag>> = remember { mutableStateOf(bookmark.tags) }
    val createArchive = remember { mutableStateOf(bookmark.createArchive) }
    val createEbook = remember { mutableStateOf(bookmark.createEbook) }

    BookmarkEditorView(
        title = title,
        bookmarkEditorType = bookmarkEditorType,
        newTag = newTag,
        assignedTags = assignedTags,
        availableTags = availableTags,
        saveBookmark = {
            when (bookmarkEditorType) {
                BookmarkEditorType.ADD -> {
                    bookmarkViewModel.saveBookmark(
                        url = bookmark.url,
                        tags = assignedTags.value,
                        createArchive = createArchive.value,
                        makeArchivePublic = makeArchivePublic,
                        createEbook = createEbook.value
                    )
                }

                BookmarkEditorType.EDIT -> {
                    bookmarkViewModel.editBookmark(
                        bookmark = bookmark.copy(
                            tags = assignedTags.value,
                            createArchive = createArchive.value,
                            public = if (makeArchivePublic) 1 else 0
                        )
                    )
                }
            }
        },
        onBackClick = onBack,
        createArchive = createArchive,
        makeArchivePublic = makeArchivePublic,
        onMakeArchivePublicChanged = {
            bookmarkViewModel.setMakeArchivePublic(value = it)
        },
        createEbook = createEbook,
        url = bookmark.url
    )

    if (bookmarkUiState.data != null) {
        updateBookmark(bookmarkUiState.data)
        showToast("Bookmark saved")
    }
}


