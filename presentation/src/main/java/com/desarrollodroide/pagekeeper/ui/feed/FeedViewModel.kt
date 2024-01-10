package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
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
import com.desarrollodroide.domain.usecase.DeleteBookmarkUseCase
import com.desarrollodroide.model.Bookmark
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class FeedViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
) : ViewModel() {

    private val _bookmarksUiState = MutableStateFlow(UiState<List<Bookmark>>(idle = true))
    val bookmarksUiState = _bookmarksUiState.asStateFlow()
    private var hasLoadedFeed = false
    private var serverUrl = ""
    private var bookmarksJob: Job? = null

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

    fun refreshUrl() {
        viewModelScope.launch {
            serverUrl = settingsPreferenceDataSource.getUrl()
        }
    }

    fun refreshFeed() {
        hasLoadedFeed = false
        getBookmarks()
    }

    fun updateBookmark(newBookmark: Bookmark) {
        _bookmarksUiState.value = _bookmarksUiState.value.copy(data = _bookmarksUiState.value.data?.map {
            if (it.url == newBookmark.url) newBookmark else it }
        )
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

    fun getUrl(bookmark: Bookmark) = if (bookmark.public == 1) "${serverUrl}/bookmark/${bookmark.id}/content" else {
        bookmark.url
    }

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
                            Log.v( "FeedViewModel","Error deleting bookmark: ${result.error?.message}")
                            _bookmarksUiState.error(errorMessage = result.error?.message ?: "Unknown error")
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

    fun getServerUrl() = serverUrl
}
