package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.domain.usecase.AddBookmarkUseCase
import com.desarrollodroide.domain.usecase.EditBookmarkUseCase
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(
    bookmarkDatabase: BookmarksDao,
    private val bookmarkAdditionUseCase: AddBookmarkUseCase,
    private val editBookmarkUseCase: EditBookmarkUseCase,
    private val userPreferences: SettingsPreferenceDataSource,

    ) : ViewModel() {

    var backendUrl = ""

    private val _bookmarkUiState = MutableStateFlow(UiState<Bookmark>(idle = true))
    val bookmarkUiState = _bookmarkUiState.asStateFlow()

    init {
        viewModelScope.launch {
            backendUrl = userPreferences.getUrl()
        }
    }

    val availableTags: StateFlow<List<Tag>> = bookmarkDatabase.getAll()
        .map { bookmarks ->
            bookmarks.flatMap { it.tags }.distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )


    fun saveBookmark(
        url: String,
        tags: List<Tag>,
        createArchive: Boolean,
        makeArchivePublic: Boolean,
        createEbook: Boolean
    ) = viewModelScope.launch {
        Log.v("Add BookmarkViewModel", "createArchive: ${createArchive} makeArchivePublic: ${makeArchivePublic}")

        viewModelScope.launch {
            bookmarkAdditionUseCase.invoke(
                serverUrl = backendUrl,
                xSession = userPreferences.getSession(),
                bookmark = Bookmark(
                    url = url,
                    tags = tags,
                    createArchive = createArchive,
                    createEbook = createEbook,
                    public = if (makeArchivePublic) 1 else 0
                )
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("Add BookmarkViewModel", result.error?.message ?: "")
                            _bookmarkUiState.error(errorMessage = result.error?.message ?: "" )
                        }

                        is Result.Loading -> {
                            Log.v("Add BookmarkViewModel", "Loading")
                            _bookmarkUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("Add BookmarkViewModel", "Success")
                            _bookmarkUiState.success(result.data)
                        }
                    }
                }
        }
    }

    fun editBookmark(bookmark: Bookmark) = viewModelScope.launch {
        viewModelScope.launch {
            editBookmarkUseCase.invoke(
                serverUrl = backendUrl,
                xSession = userPreferences.getSession(),
                bookmark = bookmark
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            val errorMessage = result.error?.message ?: result.error?.throwable?.message?: "Unknown error"
                            Log.v("Edit BookmarkViewModel", errorMessage)
                            _bookmarkUiState.error(errorMessage = errorMessage)
                        }

                        is Result.Loading -> {
                            Log.v("Edit BookmarkViewModel", "Loading")
                            _bookmarkUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("Edit BookmarkViewModel", "Success")
                            _bookmarkUiState.success(result.data)
                        }
                    }
                }
        }
    }
}