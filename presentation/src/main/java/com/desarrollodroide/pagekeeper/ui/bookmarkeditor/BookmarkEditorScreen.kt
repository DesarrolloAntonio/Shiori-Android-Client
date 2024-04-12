package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.SimpleDialog
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import org.koin.androidx.compose.get

@Composable
fun BookmarkEditorScreen(
    title: String,
    bookmarkEditorType: BookmarkEditorType,
    bookmark: Bookmark,
    onBackClick: () -> Unit,
    updateBookmark: (Bookmark) -> Unit,
    ) {
    val bookmarkViewModel = get<BookmarkViewModel>()
    val newTag = remember { mutableStateOf("") }
    val availableTags = bookmarkViewModel.availableTags.collectAsState()
    val bookmarkUiState = bookmarkViewModel.bookmarkUiState.collectAsState().value

    BackHandler {
        onBackClick()
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
            openDialog = remember { mutableStateOf(true) },
            onConfirm = { }
        )
    }

    val assignedTags: MutableState<List<Tag>> = remember { mutableStateOf(bookmark.tags) }
    val createArchive = remember { mutableStateOf(bookmark.createArchive) }
    val makeArchivePublic = remember { mutableStateOf(bookmark.public == 1) }
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
                            makeArchivePublic = makeArchivePublic.value,
                            createEbook = createEbook.value
                        )
                    }
                    BookmarkEditorType.EDIT -> {
                        bookmarkViewModel.editBookmark(bookmark = bookmark.copy(
                            tags = assignedTags.value,
                            createArchive = createArchive.value,
                            public = if (makeArchivePublic.value) 1 else 0
                        ))
                    }
                }
            },
            onBackClick = onBackClick,
            createArchive = createArchive,
            makeArchivePublic = makeArchivePublic,
            createEbook = createEbook,
            url = bookmark.url
        )

    if (bookmarkUiState.data != null) {
        Log.v("BookmarkEditorScreen", "Success")
        SimpleDialog(
            title = "Success",
            content = "Bookmark successfully saved!",
            confirmButtonText = "Ok",
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                updateBookmark(bookmarkUiState.data)
                onBackClick()
            }
        )
    }
}


