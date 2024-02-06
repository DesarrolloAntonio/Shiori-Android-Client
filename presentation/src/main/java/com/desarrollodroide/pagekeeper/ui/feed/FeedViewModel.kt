package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.idle
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.mapper.toProtoEntity
import com.desarrollodroide.domain.usecase.GetBookmarksUseCase
import com.desarrollodroide.network.model.SessionDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.domain.usecase.DeleteBookmarkUseCase
import com.desarrollodroide.domain.usecase.DownloadFileUseCase
import com.desarrollodroide.domain.usecase.UpdateBookmarkCacheUseCase
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.UpdateCachePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File

class FeedViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val updateBookmarkCacheUseCase: UpdateBookmarkCacheUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
) : ViewModel() {

    private val _bookmarksUiState = MutableStateFlow(UiState<List<Bookmark>>(idle = true))
    val bookmarksUiState = _bookmarksUiState.asStateFlow()
    private val _downloadUiState = MutableStateFlow(UiState<File>(idle = true))
    val downloadUiState = _downloadUiState.asStateFlow()

    private var hasLoadedFeed = false
    private var serverUrl = ""
    private var xSessionId = ""
    private var bookmarksJob: Job? = null
    val showBookmarkEditorScreen = mutableStateOf(false)
    val showDeleteConfirmationDialog = mutableStateOf(false)
    val showSyncDialog = mutableStateOf(false)
    val bookmarkSelected = mutableStateOf<Bookmark?>(null)
    val bookmarkToDelete = mutableStateOf<Bookmark?>(null)
    val bookmarkToUpdateCache = mutableStateOf<Bookmark?>(null)


    fun getBookmarks() {
        bookmarksJob?.cancel()
        if (hasLoadedFeed) {
            return
        }
        bookmarksJob = viewModelScope.launch {
            getBookmarksUseCase.invoke(
                serverUrl = serverUrl,
                xSession = settingsPreferenceDataSource.getSession()
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("FeedViewModel", "Error: ${result.error?.message}")
                            if (result.error is Result.ErrorType.SessionExpired) {
                                _bookmarksUiState.error(
                                    errorMessage = result.error?.message ?: ""
                                )
                            } else if (result.data?.isNotEmpty() == true) {
                                _bookmarksUiState.success(result.data)
                            }
                        }

                        is Result.Loading -> {
                            Log.v("FeedViewModel", "Loading: ${result.data}")
                            if (result.data?.isNotEmpty() == true) {
                                _bookmarksUiState.success(result.data)
                            }
                        }

                        is Result.Success -> {
                            Log.v("FeedViewModel", "Success: ${result.data}")
                            if (result.data?.isNotEmpty() == true) {
                                _bookmarksUiState.success(result.data)
                                hasLoadedFeed = true
                            }
                        }
                    }
                }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            serverUrl = settingsPreferenceDataSource.getUrl()
            xSessionId = settingsPreferenceDataSource.getSession()
        }
    }

    fun refreshFeed() {
        hasLoadedFeed = false
        getBookmarks()
    }

    fun updateBookmark(
        keepOldTitle: Boolean,
        updateArchive: Boolean,
        updateEbook: Boolean
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
                            _bookmarksUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("FeedViewModel", "Bookmark updating successfully.")
                            refreshFeed()
                        }
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
                session = SessionDTO(null, null).toProtoEntity(),
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
                            Log.v(
                                "FeedViewModel",
                                "Error deleting bookmark: ${result.error?.message}"
                            )
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
                            refreshFeed()
                        }
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
                val downloadedFile = downloadFileUseCase.execute(getEpubUrl(bookmark), bookmark.title, sessionId)
                _downloadUiState.value = UiState(data = downloadedFile)
            } catch (e: Exception) {
                Log.e("DownloadFile", "Error al descargar el archivo: ${e.message}")
                _downloadUiState.value = UiState(error = e.message)
            }
        }
    }



    fun getServerUrl() = serverUrl
    fun getSession(): String = xSessionId
}
