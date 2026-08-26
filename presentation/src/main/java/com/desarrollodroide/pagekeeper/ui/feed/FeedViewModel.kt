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
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File
import androidx.paging.cachedIn
import androidx.paging.PagingData
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.data.repository.TagsRepository
import com.desarrollodroide.data.repository.SyncStatus
import com.desarrollodroide.domain.usecase.DeleteLocalBookmarkUseCase
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.GetAllRemoteBookmarksUseCase
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
    private val syncManager: SyncWorks,
    private val bookmarksRepository: BookmarksRepository,
    private val tagsRepository: TagsRepository,

    ) : ViewModel() {

    private val _bookmarksUiState = MutableStateFlow(UiState<List<Bookmark>>(idle = true))
    val bookmarksUiState = _bookmarksUiState.asStateFlow()
    private val _downloadUiState = MutableStateFlow(UiState<File>(idle = true))
    val downloadUiState = _downloadUiState.asStateFlow()

    private val _tagsState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsState = _tagsState.asStateFlow()

    private val _currentBookmark = MutableStateFlow<Bookmark?>(null)
    val currentBookmark = _currentBookmark.asStateFlow()

    private var tagsJob: Job? = null
    private var syncJob: Job? = null
    // Read from DataStore in a coroutine but consumed from composition, so these have to be
    // observable. As plain vars the first frame saw empty strings and never recomposed: the feed's
    // images are built from serverUrl and carry the bearer token, so a cold start that rendered
    // cached rows before the preferences arrived requested them unauthenticated against no host.
    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()
    private val _xSessionId = MutableStateFlow("")
    val xSessionId: StateFlow<String> = _xSessionId.asStateFlow()
    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()
    val showBookmarkEditorScreen = mutableStateOf(false)
    // Batch edit. The web calls it that; here it starts on a long press and ends when nothing is
    // selected any more, which is what Android users expect from a list.
    private val _selectedBookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val selectedBookmarks: StateFlow<List<Bookmark>> = _selectedBookmarks.asStateFlow()

    val showDeleteConfirmationDialog = mutableStateOf(false)
    val showEpubOptionsDialog = mutableStateOf(false)
    val showSyncDialog = mutableStateOf(false)
    val bookmarkSelected = mutableStateOf<Bookmark?>(null)
    val bookmarkToDelete = mutableStateOf<Bookmark?>(null)
    val bookmarkToUpdateCache = mutableStateOf<Bookmark?>(null)
    val showOnlyHiddenTag = MutableStateFlow<Boolean>(false)
    val selectedOptionIndex = mutableStateOf(0)

    // The app bar's field writes straight into this and the feed below it re-queries, the way the
    // web does it. There is no separate search screen any more.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Waits for a pause in typing before re-querying, but clears immediately.
     *
     * A uniform debounce would leave the filtered feed on screen for another second after the
     * field is emptied, which reads as the clear button having missed.
     */
    @OptIn(FlowPreview::class)
    private fun debouncedSearchQuery() = _searchQuery
        .debounce { query -> if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
    private var isInitialized = false

    val useTwoPaneLayout: StateFlow<Boolean> = settingsPreferenceDataSource.useTwoPaneLayoutFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    /**
     * The feed, straight from Room.
     *
     * A cold flow, not a MutableStateFlow that a collector pushes into. PagingData is a single
     * shot stream: parked in a StateFlow, the copy the screen is presenting is the one it keeps,
     * and a later one never takes over. That is what left a bookmark showing "pending server
     * processing" after its create had already succeeded and the row had been replaced with the
     * server's real id. Pull to refresh did not clear it either; only relaunching did, because
     * that built a new collector.
     *
     * serverUrl is part of the query rather than read inside it: the images are built from it and
     * it arrives from DataStore after the first frame, so the query has to run again when it does.
     */
    val bookmarksState: Flow<PagingData<Bookmark>> = combine(
        selectedTags,
        showOnlyHiddenTag,
        tagToHide,
        debouncedSearchQuery(),
        _serverUrl,
    ) { selectedTags, showOnlyHidden, hiddenTag, query, serverUrl ->
        FeedQuery(selectedTags, showOnlyHidden, hiddenTag, query, serverUrl)
    }.flatMapLatest { (selectedTags, showOnlyHidden, hiddenTag, query, serverUrl) ->
        getLocalPagingBookmarksUseCase.invoke(
            serverUrl = serverUrl,
            xSession = settingsPreferenceDataSource.getSession(),
            searchText = query,
            tags = if (showOnlyHidden) emptyList() else selectedTags,
            showOnlyHiddenTag = showOnlyHidden,
            tagToHide = hiddenTag
        )
    }.cachedIn(viewModelScope)



    suspend fun initializeIfNeeded() {
        if (!isInitialized) {
            isInitialized = true
            loadInitialData()
        }
    }

    init {
        viewModelScope.launch {
            getTagsUseCase.getLocalTags()
                .distinctUntilChanged()
                .collect { localTags ->
                    Log.d(TAG, "Tags updated: ${localTags.size}")
                    if (localTags.isNotEmpty()) {
                        _tagsState.success(localTags)
                    } else {
                        _tagsState.success(emptyList())
                    }
                }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _serverUrl.value = settingsPreferenceDataSource.getUrl()
            _token.value = settingsPreferenceDataSource.getToken()
            _xSessionId.value = settingsPreferenceDataSource.getSession()
            //getLocalTags()
            if (_tagsState.value.data.isNullOrEmpty()) {
                getRemoteTags()
            }
            // An empty database means this is the first run against this account. Only the
            // timestamp is special about it: the sync itself is the same one every later start
            // does, so it is started once, below. Calling it here as well started a second full
            // paginated walk of the server, concurrently with the first, at the exact moment the
            // library is largest to fetch.
            if (bookmarkDatabase.isEmpty()) {
                settingsPreferenceDataSource.setCurrentTimeStamp()
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
                    Log.d(TAG, "Tags updated: ${localTags.size}")
                    if (localTags.isNotEmpty()) {
                        _tagsState.success(localTags)
                    } else {
                        _tagsState.success(emptyList())
                    }
                }
        }
    }

    /**
     * Walks every page of the server and writes them as they arrive.
     *
     * A sync already in flight is left to finish rather than joined by a second one. Pull to
     * refresh, editing a bookmark, adding tags to a selection and the initial load all land here,
     * and each one walks the whole library, so overlapping calls mean duplicate requests and two
     * writers on the same tables.
     */
    private fun retrieveAllRemoteBookmarks() {
        if (syncJob?.isActive == true) {
            Log.v(TAG, "Sync already running, not starting another")
            return
        }
        Log.v(TAG, "Syncing bookmarks")
        syncJob = viewModelScope.launch {
            getAllRemoteBookmarksUseCase.invoke(
                serverUrl = _serverUrl.value,
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
            //_bookmarksUiState.error(errorMessage = "Unhandled exception: ${error.message}")
        }
    }

    fun refreshFeed() {
        retrieveAllRemoteBookmarks()
    }

    /**
     * Brings one bookmark up to date without touching the rest.
     *
     * What the pending banner offers. A full sync would walk every page of the server to learn
     * about a single card; this is one request. Room is the source of truth for the feed, so
     * writing the fresh row is all that is needed for the card to redraw.
     */
    fun refreshBookmark(bookmark: Bookmark) {
        if (bookmark.id in _refreshingBookmarks.value) return
        viewModelScope.launch {
            _refreshingBookmarks.update { it + bookmark.id }
            try {
                // Three outcomes, and the two that change nothing on screen have to say so or
                // the button reads as dead: the server could not be reached, the server has
                // nothing new yet, or the card updates on its own from Room.
                runCatching {
                    bookmarksRepository.refreshBookmark(
                        xSession = _xSessionId.value,
                        serverUrl = _serverUrl.value,
                        bookmark = bookmark,
                    )
                }.onSuccess { refreshed ->
                    when {
                        refreshed == null ->
                            _transientMessage.value = "The server does not have this bookmark yet"

                        // Asked, and the server has it with nothing on it. That is as much as can
                        // ever be learned: it may be mid scrape or it may be a page it can never
                        // read, and nothing in the response distinguishes them. Stop offering to
                        // ask again, so the banner cannot sit there for ever the way it did on
                        // https://aaaa.pd. A sync that later brings real metadata clears the card
                        // on its own.
                        refreshed.isPendingServerProcessing -> {
                            _settledEmptyBookmarks.update { it + bookmark.id }
                            _transientMessage.value = "The server has nothing for this one"
                        }
                    }
                }.onFailure {
                    Log.e(TAG, "Could not refresh bookmark ${bookmark.id}", it)
                    _transientMessage.value = "Could not reach the server"
                }
            } finally {
                _refreshingBookmarks.update { it - bookmark.id }
            }
        }
    }

    /** Bookmarks with a check in flight, so their banner can show it is doing something. */
    private val _refreshingBookmarks = MutableStateFlow<Set<Int>>(emptySet())
    val refreshingBookmarks: StateFlow<Set<Int>> = _refreshingBookmarks.asStateFlow()

    /**
     * Bookmarks the server has already been asked about and had nothing for.
     *
     * Their banner comes down: there is nothing more to ask. Kept for the session rather than
     * stored, because a later sync that brings real metadata settles it properly and a restart
     * costs one more tap at worst.
     */
    private val _settledEmptyBookmarks = MutableStateFlow<Set<Int>>(emptySet())
    val settledEmptyBookmarks: StateFlow<Set<Int>> = _settledEmptyBookmarks.asStateFlow()

    private val _transientMessage = MutableStateFlow<String?>(null)
    val transientMessage: StateFlow<String?> = _transientMessage.asStateFlow()

    fun consumeTransientMessage() {
        _transientMessage.value = null
    }

    fun getRemoteTags() {
        tagsJob?.cancel()
        tagsJob =  viewModelScope.launch {
            getTagsUseCase.invoke(
                serverUrl = _serverUrl.value,
                token = _token.value,
            )
                .distinctUntilChanged()
                .collect() { result ->
                when (result) {
                    is Result.Error -> {
                        Log.v(TAG, "Error getting tags: ${result.error?.message}")
                    }
                    is Result.Loading -> {
                        Log.v(TAG, "Loading, updating tags from cache...")
                        _tagsState.success(result.data)
                    }
                    is Result.Success -> {
                        Log.v(TAG, "Tags loaded successfully.")
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

    fun updateBookmarkCache(
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
                bookmark = bookmarkToUpdateCache.value ?: return@launch,
                updateCachePayload = updateCachePayload
            )
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
        if (bookmark.public == 1) "${_serverUrl.value.removeTrailingSlash()}/bookmark/${bookmark.id}/content" else {
            bookmark.url
        }

    fun getEpubUrl(bookmark: Bookmark) =
        "${_serverUrl.value.removeTrailingSlash()}/bookmark/${bookmark.id}/ebook"

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            deleteBookmarkUseCase.invoke(bookmark = bookmark)
        }
    }

    fun toggleSelection(bookmark: Bookmark) {
        _selectedBookmarks.update { current ->
            if (current.any { it.id == bookmark.id }) {
                current.filterNot { it.id == bookmark.id }
            } else {
                current + bookmark
            }
        }
    }

    fun clearSelection() {
        _selectedBookmarks.value = emptyList()
    }

    /**
     * Adds tags to everything selected.
     *
     * Two things about the endpoint. It links tag ids rather than names, so a tag the user typed
     * that does not exist yet has to be created first. And it *sets* a bookmark's tags rather than
     * adding to them, so each bookmark is sent the union of what it already has and what was asked
     * for. Sending only the new ids is how this was first written, and it stripped three tags off a
     * bookmark on the live server before it was caught.
     */
    fun addTagsToSelected(tagNames: List<String>) {
        val selected = _selectedBookmarks.value
        if (selected.isEmpty() || tagNames.isEmpty()) return
        clearSelection()
        viewModelScope.launch {
            runCatching {
                val known = _tagsState.value.data.orEmpty().associateBy { it.name.lowercase() }
                val newTagIds = tagNames.mapNotNull { name -> known[name]?.id ?: createTag(name) }
                if (newTagIds.isEmpty()) error("None of those tags could be created")

                selected.forEach { bookmark ->
                    bookmarksRepository.addTagsToBookmarks(
                        token = _token.value,
                        serverUrl = _serverUrl.value,
                        bookmarkIds = listOf(bookmark.id),
                        tagIds = (bookmark.tags.map { it.id } + newTagIds).distinct(),
                    )
                }
                // The tag list still has to come back from the server, because tags typed here
                // may not have existed before. The bookmarks themselves do not: the bulk endpoint
                // returns each updated bookmark and those are written to Room as they arrive, so
                // a full sync would only re-fetch what was just stored.
                getRemoteTags()
            }.onFailure {
                Log.e(TAG, "Could not add tags to the selection", it)
                _bookmarksUiState.error(errorMessage = it.message ?: "Could not add tags")
            }
        }
    }

    private suspend fun createTag(name: String): Int? {
        var created: Int? = null
        tagsRepository.createTag(
            token = _token.value,
            serverUrl = _serverUrl.value,
            name = name,
        ).collect { result ->
            if (result is Result.Success) created = result.data?.id
        }
        return created
    }

    /**
     * Deletes everything selected, one call each.
     *
     * The API takes a list of ids, but the local side of a delete is per bookmark, so this reuses
     * the single delete rather than growing a second path that would have to be kept in step.
     */
    fun deleteSelected() {
        val selected = _selectedBookmarks.value
        clearSelection()
        selected.forEach { deleteLocalBookmark(it) }
    }

    /** Same reasoning as deleteSelected: reuse the single bookmark path once per selection. */
    fun updateCacheForSelected(
        keepOldTitle: Boolean,
        updateArchive: Boolean,
        updateEbook: Boolean,
    ) {
        val selected = _selectedBookmarks.value
        clearSelection()
        selected.forEach { bookmark ->
            viewModelScope.launch {
                updateBookmarkCacheUseCase.invoke(
                    bookmark = bookmark,
                    updateCachePayload = UpdateCachePayload(
                        ids = listOf(bookmark.id),
                        createArchive = updateArchive,
                        createEbook = updateEbook,
                        keepMetadata = keepOldTitle,
                        skipExist = false,
                    ),
                )
            }
        }
    }

    fun deleteLocalBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            deleteLocalBookmarkUseCase(bookmark).collect { result ->
                if (result is Result.Success) {
                    deleteBookmark(bookmark = bookmark)
                    // TODO
                } else if (result is Result.Error){
                    Log.v(TAG, "Error deleting local bookmark: ${result.error?.message}")
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

    fun getServerUrl(): String = _serverUrl.value

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

    fun retryAllPendingJobs() {
        viewModelScope.launch {
            syncManager.retryAllPendingJobs()
        }
    }

    fun loadBookmarkById(id: Int) {
        viewModelScope.launch {
            _currentBookmark.value = bookmarkDatabase.getBookmarkById(id)?.toDomainModel()
        }
    }

    private companion object {
        const val TAG = "FeedViewModel"
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

/** The four things that decide which bookmarks the feed shows. */
private data class FeedQuery(
    val selectedTags: List<Tag>,
    val showOnlyHiddenTag: Boolean,
    val tagToHide: Tag?,
    val searchText: String,
    val serverUrl: String,
)
