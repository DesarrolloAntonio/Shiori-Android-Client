package com.shiori.androidclient.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.androidclient.helpers.ThemeManager
import com.shiori.androidclient.ui.components.UiState
import com.shiori.androidclient.ui.components.error
import com.shiori.androidclient.ui.components.isLoading
import com.shiori.androidclient.ui.components.success
import com.shiori.common.result.Result
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.domain.usecase.SendLogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sendLogoutUseCase: SendLogoutUseCase,
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
                    }
                    is Result.Loading -> {
                        Log.v("SettingsViewModel", "Loading: ${result.data}")
                        _settingsUiState.isLoading(true)
                    }
                    is Result.Success -> {
                        Log.v("SettingsViewModel", "Success: ${result.data}")
                        _settingsUiState.success(result.data)
                        settingsPreferenceDataSource.resetUser()
                    }
                }
            }
        }
    }
    fun isDarkTheme() = themeManager.darkTheme.value
    fun setTheme() {
        settingsPreferenceDataSource.setTheme(!themeManager.darkTheme.value)
        themeManager.darkTheme.value = !themeManager.darkTheme.value
    }

}

