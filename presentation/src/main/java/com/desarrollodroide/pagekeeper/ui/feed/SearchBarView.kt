package com.desarrollodroide.pagekeeper.ui.feed

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.desarrollodroide.model.Bookmark
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onBookmarkClick: (Bookmark) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    val filteredBookmarks = viewModel.bookmarksState.collectAsLazyPagingItems()

    Box(Modifier.fillMaxSize()) {
        // The current SearchBar takes its text field as an `inputField` slot rather than query /
        // active parameters; that overload is deprecated.
        androidx.compose.material3.SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchText,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = {
                        Toast.makeText(context, "Select bookmark from list", Toast.LENGTH_SHORT).show()
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search bookmarks") },
                    leadingIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                        }
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.resetSearch() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                )
            },
        ) {
            BookmarkSuggestions(
                bookmarks = filteredBookmarks,
                onClickSuggestion = onBookmarkClick
            )
        }
    }
}
