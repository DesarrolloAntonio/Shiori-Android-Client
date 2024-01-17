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
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sendLogoutUseCase: SendLogoutUseCase,
    private val bookmarksRepository: BookmarksRepository,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(UiState<String>(isLoading = false))
    val settingsUiState = _settingsUiState.asStateFlow()

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
    fun getThemeMode() = themeManager.themeMode.value
    fun setTheme(newMode: ThemeMode) {
        settingsPreferenceDataSource.setTheme(newMode)
        themeManager.themeMode.value = newMode
    }

}

