package com.shiori.androidclient.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.androidclient.ui.components.UiState
import com.shiori.androidclient.ui.components.error
import com.shiori.androidclient.ui.components.isLoading
import com.shiori.androidclient.ui.components.success
import com.shiori.common.result.Result
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.data.repository.SettingsRepository
import com.shiori.domain.usecase.SendLogoutUseCase
import com.shiori.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sendLogoutUseCase: SendLogoutUseCase,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(UiState<String>(isLoading = false))
    val settingsUiState = _settingsUiState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            sendLogoutUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                xSession = settingsPreferenceDataSource.getSession()
            ).collect { result ->
                // bookmarksUiState.update {
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
//            ).collect { result ->
//                result.isLoading { isLoading ->
//                    _settingsUiState.isLoading(true)
//                }.onSuccess { user ->
//                    if (user?.status == "error") {
//                        _settingsUiState.error(user.msg ?: "Unknown error")
//                    } else if (user != null) {
//                        _settingsUiState.success(user)
//                        settingsRepository.clearUser()
//                    } else {
//                        _settingsUiState.error("Feed are null")
//                    }
//
////
////                    if (posts?.status == "error" && posts != null){
////                        _settingsUiState.update { it.copy(error = posts.msg) }
////                        settingsRepository.clearUser()
////                    } else {
////                        //_feedUiState.update {  it.copy( posts = posts) }
////                    }
//                }.onFailure { error ->
//                    _settingsUiState.update { it.copy(error = error.localizedMessage) }
//                }
//
//
//            }
        }
    }
}

