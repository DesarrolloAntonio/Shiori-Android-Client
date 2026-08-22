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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val getPagingBookmarksUseCase: GetLocalPagingBookmarksUseCase,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    ) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /**
     * Results for whatever is in the box.
     *
     * PagingData used to be pushed into a MutableStateFlow. It is a single shot stream: once a
     * LazyPagingItems has consumed an instance, handing the same one to a new collector produces
     * nothing. Coming back from a bookmark rebuilt the collector, it re-read the stale value, and
     * the screen showed the query with no results under it until the text was edited.
     *
     * Built from the query instead, and cachedIn so it can be presented again after the screen is
     * recreated.
     */
    val bookmarksState: Flow<PagingData<Bookmark>> = _searchQuery
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                flowOf(PagingData.empty())
            } else {
                getPagingBookmarksUseCase.invoke(
                    serverUrl = settingsPreferenceDataSource.getUrl(),
                    xSession = settingsPreferenceDataSource.getSession(),
                    searchText = query,
                    tags = emptyList(),
                )
            }
        }
        .cachedIn(viewModelScope)

    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun resetSearch() {
        _searchQuery.value = ""
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 1000L
    }
}