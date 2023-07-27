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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shiori.androidclient.extensions.openInBrowser
import com.shiori.androidclient.extensions.shareText
import com.shiori.androidclient.ui.components.ConfirmDialog
import com.shiori.androidclient.ui.components.InfiniteProgressDialog
import com.shiori.androidclient.ui.components.UiState
import com.shiori.androidclient.ui.savein.BookmarkEditorView
import com.shiori.model.Bookmark
import com.shiori.model.Tag
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        feedViewModel.getBookmarks()
    }
    val showBottomSheet = remember { mutableStateOf(false)}
    FeedContent(
        bookmarksUiState = feedViewModel.bookmarksUiState.collectAsState().value,
        goToLogin = {
            feedViewModel.resetData()
            goToLogin.invoke()
        },
        onPostClick = {
            Log.v("FeedContent", feedViewModel.getUrl(it))
            feedViewModel.getUrl(it).openInBrowser(context)
        },
        serverURL = feedViewModel.serverUrl.collectAsState().value,
        onPullToRefresh = {
            feedViewModel.refreshFeed()
        },
        onClickEdit = {
            showBottomSheet.value = true
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
    if (showBottomSheet.value) {
        BottomSheetDialogMaterial3()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetDialogMaterial3(
    uniqueCategories: MutableState<List<Tag>> = remember {mutableStateOf(emptyList()) },
    ){
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(key1 = scaffoldState) {
        scaffoldState.bottomSheetState.expand()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BookmarkEditorView(
                    url = "uniqueCategories",
                    assignedTags = uniqueCategories,
                    saveBookmark = {},
                    availableTags = remember { mutableStateOf(emptyList())},
                    newTag = remember { mutableStateOf("") }
                )
                Button(
                    onClick = {
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    }
                ) {
                    Text("Click to collapse sheet")
                }
            }
        }) { innerPadding ->
//        Box(Modifier.padding(innerPadding)) {
//            //Text("Scaffold Content")
//        }
    }
}


@Composable
private fun FeedContent(
    goToLogin: () -> Unit,
    onPostClick: (Bookmark) -> Unit,
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
                                onPostClick.invoke(it)
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

