package com.desarrollodroide.pagekeeper.ui.feed

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.desarrollodroide.model.Bookmark
import org.koin.androidx.compose.getViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchBar(
    onBookmarkClick: (Bookmark) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SearchViewModel = getViewModel()
) {
    val searchText by viewModel.searchQuery.collectAsState()
    val isActive = rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    val filteredBookmarks = viewModel.bookmarksState.collectAsLazyPagingItems()
    Box(
        Modifier
        .fillMaxSize()) {
        androidx.compose.material3.SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter),
            query = searchText,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = {
                Toast.makeText(context, "Select bookmark from list", Toast.LENGTH_SHORT).show()
            },
            active = isActive.value,
            onActiveChange = { isActive.value = it },
            placeholder = { Text("Search...") },
            leadingIcon = {
                IconButton(onClick = {
                    onDismiss()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                }
            },
            trailingIcon = {
                Row() {
                    Box(modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            viewModel.resetSearch()
                        }) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                    }
                }
            },
        ) {
            BookmarkSuggestions(
                bookmarks = filteredBookmarks,
                onClickSuggestion = onBookmarkClick
            )
        }
    }
}