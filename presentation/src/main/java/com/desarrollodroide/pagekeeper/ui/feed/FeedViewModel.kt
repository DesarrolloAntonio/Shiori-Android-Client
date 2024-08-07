package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.idle
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.mapper.toProtoEntity
import com.desarrollodroide.network.model.SessionDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.domain.usecase.DeleteBookmarkUseCase
import com.desarrollodroide.domain.usecase.DownloadFileUseCase
import com.desarrollodroide.domain.usecase.GetPagingBookmarksUseCase
import com.desarrollodroide.domain.usecase.UpdateBookmarkCacheUseCase
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.UpdateCachePayload
import com.desarrollodroide.pagekeeper.ui.components.isUpdating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.runBlocking
import java.io.File
import androidx.paging.cachedIn
import androidx.paging.PagingData
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.pagekeeper.ui.components.success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class FeedViewModel(
    bookmarkDatabase: BookmarksDao,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val getTagsUseCase: GetTagsUseCase,
    private val getPagingBookmarksUseCase: GetPagingBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val updateBookmarkCacheUseCase: UpdateBookmarkCacheUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
) : ViewModel() {

    private val _bookmarksUiState = MutableStateFlow(UiState<List<Bookmark>>(idle = true))
    val bookmarksUiState = _bookmarksUiState.asStateFlow()
    private val _downloadUiState = MutableStateFlow(UiState<File>(idle = true))
    val downloadUiState = _downloadUiState.asStateFlow()

    private val _bookmarksState: MutableStateFlow<PagingData<Bookmark>> =
        MutableStateFlow(value = PagingData.empty())
    val bookmarksState: MutableStateFlow<PagingData<Bookmark>> get() = _bookmarksState

    private val _tagsState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsState = _tagsState.asStateFlow()

    private var tagsJob: Job? = null
    private var hasLoadedFeed = false
    private var serverUrl = ""
    private var xSessionId = ""
    private var token = ""
    private var isLegacyApi = false
    val showBookmarkEditorScreen = mutableStateOf(false)
    val showDeleteConfirmationDialog = mutableStateOf(false)
    val showEpubOptionsDialog = mutableStateOf(false)
    val showSyncDialog = mutableStateOf(false)
    val bookmarkSelected = mutableStateOf<Bookmark?>(null)
    val bookmarkToDelete = mutableStateOf<Bookmark?>(null)
    val bookmarkToUpdateCache = mutableStateOf<Bookmark?>(null)
    val isCompactView = MutableStateFlow<Boolean>(false)
    val tagToHide = MutableStateFlow<Tag?>(null)

    val availableTags: StateFlow<List<Tag>> = bookmarkDatabase.getAll()
        .map { bookmarks ->
            bookmarks.flatMap { it.tags }.distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )

    suspend fun getPagingBookmarks(
        tags: List<Tag> = emptyList(),
    ) {
        getPagingBookmarksUseCase.invoke(
            serverUrl = serverUrl,
            xSession = settingsPreferenceDataSource.getSession(),
            tags = tags,
            saveToLocal = tags.isEmpty()
        )
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
            .collect { loadResult: PagingData<Bookmark> ->
                bookmarksState.value = loadResult
            }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            serverUrl = settingsPreferenceDataSource.getUrl()
            token = settingsPreferenceDataSource.getToken()
            xSessionId = settingsPreferenceDataSource.getSession()
            isLegacyApi = settingsPreferenceDataSource.getIsLegacyApi()
            isCompactView.value = settingsPreferenceDataSource.getCompactView()
            tagToHide.value = settingsPreferenceDataSource.getHideTag()
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            hasLoadedFeed = false
            getPagingBookmarks()
        }
    }

    fun getTags() {
        tagsJob?.cancel()
        tagsJob =  viewModelScope.launch {
            getTagsUseCase.invoke(
                serverUrl = serverUrl,
                token = token,
            )
                .distinctUntilChanged()
                .collect() { result ->
                when (result) {
                    is Result.Error -> {
                        Log.v("FeedViewModel", "Error getting tags: ${result.error?.message}")
                    }
                    is Result.Loading -> {
                        Log.v("FeedViewModel", "Loading, updating tags from cache...")
                        _tagsState.success(result.data)
                    }
                    is Result.Success -> {
                        Log.v("FeedViewModel", "Tags loaded successfully.")
                        _tagsState.success(result.data)
                    }
                }
            }
        }
    }

    fun handleLoadState(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            _bookmarksUiState.update { currentState ->
                currentState.copy(isLoading = false, error = loadState.error.message)
            }
        }
    }

    fun updateBookmark(
        keepOldTitle: Boolean,
        updateArchive: Boolean,
        updateEbook: Boolean,
    ) {
        val updateCachePayload = UpdateCachePayload(
            ids = listOf(bookmarkToUpdateCache.value?.id ?: -1),
            createArchive = updateArchive,
            createEbook = updateEbook,
            keepMetadata = keepOldTitle,
            skipExist = false
        )

        viewModelScope.launch {
            updateBookmarkCacheUseCase.invoke(
                serverUrl = serverUrl,
                xSession = settingsPreferenceDataSource.getSession(),
                token = token,
                isLegacyApi = settingsPreferenceDataSource.getIsLegacyApi(),
                updateCachePayload = updateCachePayload
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v(
                                "FeedViewModel",
                                "Error updating bookmark: ${result.error?.message}"
                            )
                            _bookmarksUiState.error(
                                errorMessage = result.error?.message ?: "Unknown error"
                            )
                        }

                        is Result.Loading -> {
                            Log.v("FeedViewModel", "updating bookmark...")
                            _bookmarksUiState.isUpdating(true)
                        }

                        is Result.Success -> {
                            Log.v("FeedViewModel", "Bookmark updating successfully.")
                            _bookmarksUiState.isUpdating(false)
                            refreshFeed()
                        }

                        else -> {}
                    }
                }
        }
    }


    fun resetData() {
        _bookmarksUiState.idle(true)
        hasLoadedFeed = false
        viewModelScope.launch {
            settingsPreferenceDataSource.saveUser(
                password = "",
                session = SessionDTO(null, null, null).toProtoEntity(),
                serverUrl = ""
            )
        }
    }

    fun getUrl(bookmark: Bookmark) =
        if (bookmark.public == 1) "${serverUrl.removeTrailingSlash()}/bookmark/${bookmark.id}/content" else {
            bookmark.url
        }

    fun getEpubUrl(bookmark: Bookmark) =
        "${serverUrl.removeTrailingSlash()}/bookmark/${bookmark.id}/ebook"

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            deleteBookmarkUseCase.invoke(
                serverUrl = serverUrl,
                xSession = settingsPreferenceDataSource.getSession(),
                bookmark = bookmark
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("FeedViewModel","Error deleting bookmark: ${result.error?.message}")
                            _bookmarksUiState.error(
                                errorMessage = result.error?.message ?: "Unknown error"
                            )
                        }
                        is Result.Loading -> {
                            Log.v("FeedViewModel", "Deleting bookmark...")
                            _bookmarksUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("FeedViewModel", "Bookmark deleted successfully.")
                            _bookmarksUiState.isLoading(false)
                            refreshFeed()
                        }
                        else -> {}
                    }
                }
        }
    }

    fun downloadFile(
        bookmark: Bookmark,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sessionId = settingsPreferenceDataSource.getSession()
            _downloadUiState.value = UiState(isLoading = true)
            try {
                val downloadedFile =
                    downloadFileUseCase.execute(getEpubUrl(bookmark), bookmark.title, sessionId)
                _downloadUiState.value = UiState(data = downloadedFile)
                showEpubOptionsDialog.value = true
            } catch (e: Exception) {
                Log.e("DownloadFile", "Error al descargar el archivo: ${e.message}")
                _downloadUiState.value = UiState(error = e.message)
            }
        }
    }

    fun getServerUrl() = serverUrl
    fun getSession(): String = xSessionId

    fun getToken(): String = runBlocking {
        settingsPreferenceDataSource.getToken()
    }

    fun isLegacyApi(): Boolean = runBlocking {
        settingsPreferenceDataSource.getIsLegacyApi()
    }

}
