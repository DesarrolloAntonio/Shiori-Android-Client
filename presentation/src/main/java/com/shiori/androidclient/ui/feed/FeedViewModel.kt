package com.shiori.androidclient.ui.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.androidclient.ui.components.UiState
import com.shiori.androidclient.ui.components.error
import com.shiori.androidclient.ui.components.idle
import com.shiori.androidclient.ui.components.isLoading
import com.shiori.androidclient.ui.components.success
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.data.mapper.toProtoEntity
import com.shiori.domain.usecase.GetBookmarksUseCase
import com.shiori.network.model.SessionDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.shiori.common.result.Result
import com.shiori.domain.usecase.DeleteBookmarkUseCase
import com.shiori.model.Bookmark

class FeedViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
) : ViewModel() {

    private val _bookmarksUiState = MutableStateFlow(UiState<List<Bookmark>>(idle = true))
    val bookmarksUiState = _bookmarksUiState.asStateFlow()
    private var hasLoadedFeed = false
    val serverUrl = MutableStateFlow<String>("")

    init {
        viewModelScope.launch {
            serverUrl.emit(settingsPreferenceDataSource.getUrl())
        }
    }

    fun getBookmarks() {
        if (hasLoadedFeed) {
            return
        }
        viewModelScope.launch {

            getBookmarksUseCase.invoke(
                serverUrl = serverUrl.value,
                xSession = settingsPreferenceDataSource.getSession()
            )
                .collect { result ->
                    hasLoadedFeed = true
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
                            _bookmarksUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("FeedViewModel", "Success: ${result.data}")
                            _bookmarksUiState.success(result.data)
                        }
                    }
                }
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

    fun getUrl(bookmark: Bookmark) = if (bookmark.public == 1) "${serverUrl.value}/bookmark/${bookmark.id}/content" else {
        bookmark.url
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            deleteBookmarkUseCase.invoke(
                serverUrl = serverUrl.value,
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
}
