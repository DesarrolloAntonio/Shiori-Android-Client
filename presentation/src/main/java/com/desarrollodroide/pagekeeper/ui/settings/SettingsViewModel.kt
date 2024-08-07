package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sendLogoutUseCase: SendLogoutUseCase,
    private val bookmarksRepository: BookmarksRepository,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val themeManager: ThemeManager,
    private val getTagsUseCase: GetTagsUseCase
) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(UiState<String>(isLoading = false))
    val settingsUiState = _settingsUiState.asStateFlow()

    private val _tagsState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsState = _tagsState.asStateFlow()

    private val _tagToHide = MutableStateFlow<Tag?>(null)
    val tagToHide = _tagToHide.asStateFlow()

    val makeArchivePublic = MutableStateFlow<Boolean>(false)
    val createEbook = MutableStateFlow<Boolean>(false)
    val createArchive = MutableStateFlow<Boolean>(false)
    val compactView = MutableStateFlow<Boolean>(false)
    val autoAddBookmark = MutableStateFlow<Boolean>(false)
    val useDynamicColors = MutableStateFlow<Boolean>(false)
    val themeMode = MutableStateFlow<ThemeMode>(ThemeMode.AUTO)
    private var token = ""

    init {
        loadSettings()
        observeDefaultsSettings()
    }
    fun logout() {
        viewModelScope.launch {
            sendLogoutUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                xSession = settingsPreferenceDataSource.getSession()
            ).collect { result ->
                when (result) {
                    is Result.Error -> {
                        Log.v("SettingsViewModel", "Error: ${result.error?.throwable?.message}")
                        _settingsUiState.error(errorMessage = result.error?.throwable?.message?: "")
                        settingsPreferenceDataSource.resetUser()
                        bookmarksRepository.deleteAllLocalBookmarks()
                    }
                    is Result.Loading -> {
                        Log.v("SettingsViewModel", "Loading: ${result.data}")
                        _settingsUiState.isLoading(true)
                    }
                    is Result.Success -> {
                        Log.v("SettingsViewModel", "Success: ${result.data}")
                        settingsPreferenceDataSource.resetUser()
                        bookmarksRepository.deleteAllLocalBookmarks()
                        _settingsUiState.success(result.data)
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            makeArchivePublic.value = settingsPreferenceDataSource.getMakeArchivePublic()
            createEbook.value = settingsPreferenceDataSource.getCreateEbook()
            createArchive.value = settingsPreferenceDataSource.getCreateArchive()
            compactView.value = settingsPreferenceDataSource.getCompactView()
            autoAddBookmark.value = settingsPreferenceDataSource.getAutoAddBookmark()
            useDynamicColors.value = settingsPreferenceDataSource.getUseDynamicColors()
            themeMode.value = settingsPreferenceDataSource.getThemeMode()
            token = settingsPreferenceDataSource.getToken()
            _tagToHide.value = settingsPreferenceDataSource.getHideTag()
        }
    }

    fun getTags() {
      viewModelScope.launch {
            getTagsUseCase.invoke(
                serverUrl = settingsPreferenceDataSource.getUrl(),
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

    fun setHideTag(tag: Tag?) {
        viewModelScope.launch {
            settingsPreferenceDataSource.setHideTag(tag)
            _tagToHide.value = tag
        }
    }

    private fun observeDefaultsSettings() {
        viewModelScope.launch {
            makeArchivePublic.collect { newValue ->
                settingsPreferenceDataSource.setMakeArchivePublic(newValue)
            }
        }
        viewModelScope.launch {
            createEbook.collect { newValue ->
                settingsPreferenceDataSource.setCreateEbook(newValue)
            }
        }
        viewModelScope.launch {
            createArchive.collect { newValue ->
                settingsPreferenceDataSource.setCreateArchive(newValue)
            }
        }
        viewModelScope.launch {
            compactView.collect { newValue ->
                settingsPreferenceDataSource.setCompactView(newValue)
            }
        }
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
        viewModelScope.launch {
            autoAddBookmark.collect { newValue ->
                settingsPreferenceDataSource.setAutoAddBookmark(newValue)
            }
        }
    }
}

