package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.pagekeeper.ui.components.UiState
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
    var autoAddBookmark: Boolean = false
        private set

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
        autoAddBookmark = settingsPreferenceDataSource.autoAddBookmarkFlow.first()
    }

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

    fun autoAddBookmark(
        url: String,
        title: String,
    ) = viewModelScope.launch {
        saveBookmark(
            url = url,
            title = title,
            tags = emptyList(),
            createArchive = createArchive,
            makeArchivePublic = makeArchivePublic,
            createEbook = createEbook
        )
    }

    fun saveBookmark(
        url: String,
        title: String,
        tags: List<Tag>,
        createArchive: Boolean,
        makeArchivePublic: Boolean,
        createEbook: Boolean
    ) = viewModelScope.launch {
        bookmarkAdditionUseCase.invoke(
            bookmark = Bookmark(
                url = url,
                title = title,
                tags = tags,
                createArchive = createArchive,
                createEbook = createEbook,
                public = if (makeArchivePublic) 1 else 0
            )
        )
    }

    fun editBookmark(bookmark: Bookmark) = viewModelScope.launch {
        viewModelScope.launch {
            editBookmarkUseCase.invoke(
                bookmark = bookmark
            )
        }
    }

    fun userHasSession() = runBlocking {
        userPreferences.getUser().first().hasSession()
    }

    private fun emitToastIfAutoAdd(message: String) {
        viewModelScope.launch {
            if (autoAddBookmark) {
                _toastMessage.value = message
            }
        }
    }

}