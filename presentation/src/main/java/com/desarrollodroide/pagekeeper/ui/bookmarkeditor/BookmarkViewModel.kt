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
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.domain.usecase.AddBookmarkUseCase
import com.desarrollodroide.domain.usecase.EditBookmarkUseCase
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BookmarkViewModel(
    bookmarkDatabase: BookmarksDao,
    private val bookmarkAdditionUseCase: AddBookmarkUseCase,
    private val bookmarksRepository: BookmarksRepository,
    private val editBookmarkUseCase: EditBookmarkUseCase,
    private val userPreferences: SettingsPreferenceDataSource,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
) : ViewModel() {

    var backendUrl = ""
    var sessionExpired = false

    private val _bookmarkUiState = MutableStateFlow(UiState<Bookmark>(idle = true))
    val bookmarkUiState = _bookmarkUiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            backendUrl = userPreferences.getUrl()
            initializePreferences()
        }
    }

    private suspend fun initializePreferences() {
        makeArchivePublic = settingsPreferenceDataSource.makeArchivePublicFlow.first()
        createEbook = settingsPreferenceDataSource.createEbookFlow.first()
        createArchive = settingsPreferenceDataSource.createArchiveFlow.first()
    }

    val autoAddBookmark: StateFlow<Boolean> = settingsPreferenceDataSource.autoAddBookmarkFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    var makeArchivePublic: Boolean = false
    var createEbook: Boolean = false
    var createArchive: Boolean = false

    val availableTags: StateFlow<List<Tag>> = bookmarkDatabase.getAll()
        .map { bookmarks ->
            bookmarks.flatMap { it.tags }.distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )

    fun autoAddBookmark(url: String) = viewModelScope.launch {
        saveBookmark(
            url = url,
            tags = emptyList(),
            createArchive = createArchive,
            makeArchivePublic = makeArchivePublic,
            createEbook = createEbook
        )
    }

    fun saveBookmark(
        url: String,
        tags: List<Tag>,
        createArchive: Boolean,
        makeArchivePublic: Boolean,
        createEbook: Boolean
    ) = viewModelScope.launch {
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
                        if (result.error is Result.ErrorType.SessionExpired) {
                            settingsPreferenceDataSource.resetUser()
                            bookmarksRepository.deleteAllLocalBookmarks()
                            sessionExpired = true
                            _bookmarkUiState.error(
                                errorMessage = result.error?.message ?: ""
                            )
                            emitToastIfAutoAdd(result.error?.message ?: "")
                        } else {
                            Log.v("Add BookmarkViewModel", result.error?.message ?: "")
                            _bookmarkUiState.error(
                                errorMessage = result.error?.message ?: "Unknown error"
                            )
                            emitToastIfAutoAdd("Error: ${result.error?.message ?: "Unknown error"}")
                        }
                    }

                    is Result.Loading -> {
                        Log.v("Add BookmarkViewModel", "Loading")
                        _bookmarkUiState.isLoading(true)
                    }

                    is Result.Success -> {
                        Log.v("Add BookmarkViewModel", "Success")
                        _bookmarkUiState.success(result.data)
                        emitToastIfAutoAdd("Bookmark saved successfully")
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
                            val errorMessage =
                                result.error?.message ?: result.error?.throwable?.message
                                ?: "Unknown error"
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

    fun userHasSession() = runBlocking {
        userPreferences.getUser().first().hasSession()
    }

    private fun emitToastIfAutoAdd(message: String) {
        viewModelScope.launch {
            if (autoAddBookmark.value) {
                _toastMessage.value = message
            }
        }
    }

}