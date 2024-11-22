package com.desarrollodroide.pagekeeper.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.domain.usecase.GetLocalPagingBookmarksUseCase
import com.desarrollodroide.model.Bookmark
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val getPagingBookmarksUseCase: GetLocalPagingBookmarksUseCase,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    ) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _bookmarksState: MutableStateFlow<PagingData<Bookmark>> = MutableStateFlow(value = PagingData.empty())
    val bookmarksState: MutableStateFlow<PagingData<Bookmark>> get() = _bookmarksState

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(1000)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotEmpty()) {
                        getPagingBookmarks(query)
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    suspend fun getPagingBookmarks(
        searchText: String
    ) {
        _bookmarksState.value = PagingData.empty()
        getPagingBookmarksUseCase.invoke(
            serverUrl = settingsPreferenceDataSource.getUrl(),
            xSession = settingsPreferenceDataSource.getSession(),
            searchText = searchText,
            tags = emptyList(),
        )
            .cachedIn(viewModelScope)
            .collect {
                _bookmarksState.value = it
            }
    }

    fun resetSearch() {
        _searchQuery.value = ""
    }
}