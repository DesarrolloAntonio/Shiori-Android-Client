package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.extensions.bytesToDisplaySize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sendLogoutUseCase: SendLogoutUseCase,
    private val bookmarksRepository: BookmarksRepository,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val themeManager: ThemeManager,
    private val getTagsUseCase: GetTagsUseCase,
    private val imageLoader: ImageLoader,
    ) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(UiState<String>(isLoading = false))
    val settingsUiState = _settingsUiState.asStateFlow()

    private val _tagsState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsState = _tagsState.asStateFlow()

    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    val useDynamicColors = MutableStateFlow(false)
    val themeMode = MutableStateFlow(ThemeMode.AUTO)
    private var token = ""

    val compactView: StateFlow<Boolean> = settingsPreferenceDataSource.compactViewFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val makeArchivePublic: StateFlow<Boolean> = settingsPreferenceDataSource.makeArchivePublicFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val createEbook: StateFlow<Boolean> = settingsPreferenceDataSource.createEbookFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoAddBookmark: StateFlow<Boolean> = settingsPreferenceDataSource.autoAddBookmarkFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val createArchive: StateFlow<Boolean> = settingsPreferenceDataSource.createArchiveFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val tagToHide: StateFlow<Tag?> = settingsPreferenceDataSource.hideTagFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    fun setAutoAddBookmark(value: Boolean) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setAutoAddBookmark(value)
        }
    }

    fun setCompactView(isCompact: Boolean) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setCompactView(isCompact)
        }
    }

    fun setMakeArchivePublic(isPublic: Boolean) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setMakeArchivePublic(isPublic)
        }
    }

    fun setCreateEbook(ebook: Boolean) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setCreateEbook(ebook)
        }
    }

    fun setCreateArchive(archive: Boolean) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setCreateArchive(archive)
        }
    }

    fun setHideTag(tag: Tag?) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setHideTag(tag)
        }
    }

    init {
        loadSettings()
        observeDefaultsSettings()
        updateCacheSize()
    }
    fun logout() {
        viewModelScope.launch {
            sendLogoutUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                xSession = settingsPreferenceDataSource.getSession()
            ).collect { result ->
                when (result) {
                    is Result.Error -> {
                        _settingsUiState.error(errorMessage = result.error?.throwable?.message?: "")
                    }
                    is Result.Loading -> {
                        _settingsUiState.isLoading(true)
                    }
                    is Result.Success -> {
                        _settingsUiState.success(result.data)
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            useDynamicColors.value = settingsPreferenceDataSource.getUseDynamicColors()
            themeMode.value = settingsPreferenceDataSource.getThemeMode()
            token = settingsPreferenceDataSource.getToken()
        }
    }

    fun getTags() {
      viewModelScope.launch {
            getTagsUseCase.invoke(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                token = token,
            )
                .distinctUntilChanged()
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("FeedViewModel", "Error getting tags: ${result.error?.message}")
                        }
                        is Result.Loading -> {
                            Log.v("FeedViewModel", "Loading, updating tags from cache...")
                            _tagsState.isLoading(true)
                        }
                        is Result.Success -> {
                            Log.v("FeedViewModel", "Tags loaded successfully.")
                            _tagsState.success(result.data)
                        }
                    }
                }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun updateCacheSize() {
        viewModelScope.launch {
            val size = imageLoader.diskCache?.size ?: 0L
            _cacheSize.value = size.bytesToDisplaySize()
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    fun clearImageCache() {
        viewModelScope.launch {
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
            updateCacheSize()
        }
    }

    private fun observeDefaultsSettings() {
        viewModelScope.launch {
            useDynamicColors.collect { newValue ->
                settingsPreferenceDataSource.setUseDynamicColors(newValue)
                themeManager.useDynamicColors.value = newValue
            }
        }
        viewModelScope.launch {
            themeMode.collect { newValue ->
                settingsPreferenceDataSource.setTheme(newValue)
                themeManager.themeMode.value = newValue
            }
        }
    }
}

