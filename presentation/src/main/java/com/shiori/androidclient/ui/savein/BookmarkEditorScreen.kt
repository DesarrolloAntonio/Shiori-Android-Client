package com.shiori.androidclient.ui.savein

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.shiori.androidclient.ui.components.ConfirmDialog
import com.shiori.androidclient.ui.components.InfiniteProgressDialog
import com.shiori.androidclient.ui.components.SimpleDialog
import com.shiori.model.Tag
import org.koin.androidx.compose.get

@Composable
fun BookmarkEditorScreen(
    assignedTags: MutableState<List<Tag>>,
    sharedUrl: String,
    onFinishActivity: () -> Unit,
) {
    val viewModel = get<BookmarkViewModel>()
    val newTag = remember { mutableStateOf("") }
    val availableTags = viewModel.availableTags.collectAsState()
    val bookmarkUiState = viewModel.bookmarkUiState.collectAsState().value

    if (bookmarkUiState.isLoading) {
        Log.v("BookmarkEditorScreen", "isLoading")
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!bookmarkUiState.error.isNullOrEmpty()) {
        Log.v("BookmarkEditorScreen", "Error")
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = bookmarkUiState.error?:"",
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                //viewModel.clearError()
            }
        )
    }


    //if (bookmarkUiState.value.data == null && !bookmarkUiState.value.idle) {
        BookmarkEditorView(
            newTag = newTag,
            assignedTags = assignedTags,
            availableTags = availableTags,
            saveBookmark = {
                viewModel.saveBookmark(sharedUrl, assignedTags.value)
                           },
        )
    //}

    if (bookmarkUiState.data != null) {
        Log.v("BookmarkEditorScreen", "Success")
        SimpleDialog(
            title = "Success",
            content = "Bookmark successfully saved!",
            confirmButtonText = "Ok",
            openDialog = remember { mutableStateOf(true) },
            onConfirm = onFinishActivity
        )
    }
}


