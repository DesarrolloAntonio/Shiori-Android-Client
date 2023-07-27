package com.shiori.androidclient.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.androidclient.ui.components.UiState
import com.shiori.androidclient.ui.components.error
import com.shiori.androidclient.ui.components.isLoading
import com.shiori.androidclient.ui.components.success
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.domain.usecase.SendLoginUseCase
import com.shiori.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.shiori.common.result.Result

class LoginViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val loginUseCase: SendLoginUseCase,
) : ViewModel() {

    var rememberSession = mutableStateOf(false)
    var userName = mutableStateOf(TextFieldValue("Memnoch"))
    var password = mutableStateOf(TextFieldValue("e%dPd3&eAV@#v7TKP%NvmZ5"))
    var serverUrl = mutableStateOf(TextFieldValue("http://144.24.174.227:49153"))
    val userNameError = mutableStateOf(false)
    val passwordError = mutableStateOf(false)
    val urlError = mutableStateOf(false)

    private val _loginUiState = MutableStateFlow(UiState<User>(idle = true))
    val loginUiState = _loginUiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUser()
        }
    }

    fun sendLogin() {
        viewModelScope.launch {
            loginUseCase.invoke(
                username = userName.value.text,
                password = password.value.text,
                serverUrl = serverUrl.value.text
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            settingsPreferenceDataSource.resetUser()
                            _loginUiState.error(
                                errorMessage = result.error?.throwable?.message ?: ""
                            )
                        }

                        is Result.Loading -> {
                            _loginUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            if (result.data != null && result.data?.hasSession() == true) {
                                _loginUiState.success(result.data)
                            } else {
                                settingsPreferenceDataSource.resetUser()
                            }
                        }
                    }
                }
        }
    }

    fun clearError() {
        _loginUiState.success(null)
    }

    private suspend fun getUser() {
        //clearError()
        val user = settingsPreferenceDataSource.getUser().first()
        if (user.hasSession()) {
            _loginUiState.success(user)
        } else {
            _loginUiState.success(null)
        }
    }
}
