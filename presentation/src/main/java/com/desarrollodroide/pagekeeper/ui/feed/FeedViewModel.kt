package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.idle
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
import com.desarrollodroide.domain.usecase.GetLocalPagingBookmarksUseCase
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
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.data.repository.SyncStatus
import com.desarrollodroide.domain.usecase.DeleteLocalBookmarkUseCase
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.SyncBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetAllRemoteBookmarksUseCase
import com.desarrollodroide.model.SyncBookmarksRequestPayload
import com.desarrollodroide.model.SyncBookmarksResponse
import com.desarrollodroide.pagekeeper.ui.components.success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class FeedViewModel(
    private val bookmarkDatabase: BookmarksDao,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val getTagsUseCase: GetTagsUseCase,
    private val getLocalPagingBookmarksUseCase: GetLocalPagingBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val updateBookmarkCacheUseCase: UpdateBookmarkCacheUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val getAllRemoteBookmarksUseCase: GetAllRemoteBookmarksUseCase,
    private val deleteLocalBookmarkUseCase: DeleteLocalBookmarkUseCase,
    private val syncBookmarksUseCase: SyncBookmarksUseCase,
    private val syncManager: SyncWorks,

    ) : ViewModel() {

    private val TAG = "FeedViewModel"
    private val _bookmarksUiState = MutableStateFlow(UiState<List<Bookmark>>(idle = true))
    val bookmarksUiState = _bookmarksUiState.asStateFlow()
    private val _downloadUiState = MutableStateFlow(UiState<File>(idle = true))
    val downloadUiState = _downloadUiState.asStateFlow()
    private val _bookmarksState: MutableStateFlow<PagingData<Bookmark>> = MutableStateFlow(value = PagingData.empty())
    val bookmarksState: MutableStateFlow<PagingData<Bookmark>> get() = _bookmarksState

    private val _tagsState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsState = _tagsState.asStateFlow()

    private var tagsJob: Job? = null
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
    val showOnlyHiddenTag = MutableStateFlow<Boolean>(false)
    val selectedOptionIndex = mutableStateOf(0)
    private var isInitialized = false

    val compactView: StateFlow<Boolean> = settingsPreferenceDataSource.compactViewFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val tagToHide: StateFlow<Tag?> = settingsPreferenceDataSource.hideTagFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val selectedTags: StateFlow<List<Tag>> = combine(
        settingsPreferenceDataSource.selectedCategoriesFlow,
        _tagsState
    ) { selectedIds, tagsState ->
        val allTags = tagsState.data ?: emptyList()
        allTags.filter { it.id.toString() in selectedIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncState = MutableStateFlow<UiState<SyncBookmarksResponse>>(UiState(idle = true))
    val syncState: StateFlow<UiState<SyncBookmarksResponse>> = _syncState.asStateFlow()


    suspend fun initializeIfNeeded() {
        if (!isInitialized) {
            isInitialized = true
            loadInitialData()
        }
    }

    init {
        viewModelScope.launch {
            combine(
                selectedTags,
                showOnlyHiddenTag,
                tagToHide
            ) { selectedTags, showOnlyHidden, hiddenTag ->
                Triple(selectedTags, showOnlyHidden, hiddenTag)
            }.flatMapLatest { (selectedTags, showOnlyHidden, hiddenTag) ->
                getLocalPagingBookmarksUseCase.invoke(
                    serverUrl = serverUrl,
                    xSession = settingsPreferenceDataSource.getSession(),
                    tags = if (showOnlyHidden) emptyList() else selectedTags,
                    showOnlyHiddenTag = showOnlyHidden,
                    tagToHide = hiddenTag
                )
            }.cachedIn(viewModelScope)
                .collect { pagingData ->
                    _bookmarksState.value = pagingData
                }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            serverUrl = settingsPreferenceDataSource.getUrl()
            token = settingsPreferenceDataSource.getToken()
            xSessionId = settingsPreferenceDataSource.getSession()
            isLegacyApi = settingsPreferenceDataSource.getIsLegacyApi()
            getLocalTags()
            if (_tagsState.value.data.isNullOrEmpty()) {
                getRemoteTags()
            }
            if (bookmarkDatabase.isEmpty()) {
                settingsPreferenceDataSource.setCurrentTimeStamp()
                retrieveAllRemoteBookmarks()
            }
            refreshFeed()
        }
    }

    fun getLocalTags() {
        tagsJob?.cancel()
        tagsJob = viewModelScope.launch {
            getTagsUseCase.getLocalTags()
                .distinctUntilChanged()
                .collect { localTags ->
                    if (localTags.isNotEmpty()) {
                        _tagsState.success(localTags)
                    } else {
                        _tagsState.success(emptyList())
                    }
                }
        }
    }

    fun syncBookmarks(ids: List<Int>, lastSync: Long, page: Int = 1) {
        viewModelScope.launch {
            _syncState.value = UiState(isLoading = true)
            val syncBookmarksRequestPayload = SyncBookmarksRequestPayload(
                ids = ids,
                last_sync = lastSync,
                page = page
            )
            syncBookmarksUseCase(
                token = token,
                serverUrl = serverUrl,
                syncBookmarksRequestPayload = syncBookmarksRequestPayload
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        result.data?.let { response ->
                            Log.v(TAG, "Sync response: $response")
                            _syncState.value = UiState(data = response)
                            syncBookmarksUseCase.handleSuccessfulSync(response, lastSync)
                        } ?: run {
                            _syncState.value = UiState(error = "Sync response was null")
                            Log.e(TAG, "Sync response was null")
                        }
                    }
                    is Result.Error -> {
                        _syncState.value = UiState(error = result.error?.message)
                        Log.e(TAG, "Error syncing bookmarks: ${result.error?.message}")
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun retrieveAllRemoteBookmarks() {
        Log.v(TAG, "Syncing bookmarks")
        viewModelScope.launch {
            getAllRemoteBookmarksUseCase.invoke(
                serverUrl = serverUrl,
                xSession = settingsPreferenceDataSource.getSession()
            ).collect { result ->
                result.fold(
                    onSuccess = { status ->
                        when (status) {
                            is SyncStatus.Started -> {
                                Log.v(TAG, "Sync started")
                            }
                            is SyncStatus.InProgress -> {
                                Log.v(TAG, "Sync in progress")
                            }
                            is SyncStatus.Completed -> {
                                Log.v(TAG, "Sync completed")
                            }
                            is SyncStatus.Error -> {
                                Log.v(TAG, "Sync error")
                                if (status.error is Result.ErrorType.SessionExpired) {
                                    Log.v(TAG, "Session expired")
                                }
                                handleSyncError(status.error)
                            }
                            SyncStatus.Started -> { }
                        }
                    },
                    onFailure = { throwable ->
                        _bookmarksUiState.error(errorMessage = throwable.message.toString())
                    }
                )
            }
        }
    }

    private fun handleSyncError(error: Result.ErrorType) {
        if (error is Result.ErrorType.SessionExpired) {
            _bookmarksUiState.error(errorMessage = SESSION_HAS_BEEN_EXPIRED)
        } else {
            Log.e(TAG, "Unhandled exception: ${error.message}")
            _bookmarksUiState.error(errorMessage = "Unhandled exception: ${error.message}")
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            val localBookmarkIds = bookmarkDatabase.getAllBookmarkIds()
            syncBookmarks(localBookmarkIds, settingsPreferenceDataSource.getLastSyncTimestamp())
        }
    }

    fun getRemoteTags() {
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
        isInitialized = false
        _bookmarksUiState.idle(true)
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
            deleteBookmarkUseCase.invoke(bookmark = bookmark)
        }
    }

    fun deleteLocalBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            deleteLocalBookmarkUseCase(bookmark).collect { result ->
                if (result is Result.Success) {
                    deleteBookmark(bookmark = bookmark)
                    // TODO
                } else if (result is Result.Error){
                    Log.v("FeedViewModel","Error deleting local bookmark: ${result.error?.message}")
                    _bookmarksUiState.error(
                        errorMessage = result.error?.message ?: "Unknown error"
                    )
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

    fun addSelectedTag(tag: Tag) {
        viewModelScope.launch {
            val currentTags = selectedTags.value
            if (tag !in currentTags) {
                settingsPreferenceDataSource.addSelectedCategory(tag)
            }
        }
    }

    fun removeSelectedTag(tag: Tag) {
        viewModelScope.launch {
            settingsPreferenceDataSource.removeSelectedCategory(tag)
        }
    }

    fun resetTags() {
        viewModelScope.launch {
            settingsPreferenceDataSource.setSelectedCategories(emptyList())
        }
    }

    fun getPendingWorks() =
        syncManager.getPendingJobs()

}
