package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.domain.usecase.SendLoginUseCase
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.Account

class LoginViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val loginUseCase: SendLoginUseCase,
) : ViewModel() {

    var rememberSession = mutableStateOf(false)
    var userName = mutableStateOf(TextFieldValue("Memnoch"))
    var password = mutableStateOf(TextFieldValue("e%dPd3&eAV@#v7TKP%NvmZ5"))
    var serverUrl = mutableStateOf(TextFieldValue("http://192.168.1.206:8580/"))

//    var serverUrl = mutableStateOf(TextFieldValue(""))
//    var userName = mutableStateOf(TextFieldValue(""))
//    var password = mutableStateOf(TextFieldValue(""))

    val userNameError = mutableStateOf(false)
    val passwordError = mutableStateOf(false)
    val urlError = mutableStateOf(false)

    private val _userUiState = MutableStateFlow(UiState<User>(idle = true))
    val userUiState = _userUiState.asStateFlow()

    private val _rememberUserUiState = MutableStateFlow(UiState<Account>(idle = true))
    val rememberUserUiState = _rememberUserUiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUser()
            getRememberUser()
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
                            _userUiState.error(
                                errorMessage = result.error?.throwable?.message ?: ""
                            )
                        }

                        is Result.Loading -> {
                            _userUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            if (result.data != null && result.data?.hasSession() == true) {
                                if (rememberSession.value) {
                                    settingsPreferenceDataSource.saveRememberUser(
                                        url = serverUrl.value.text,
                                        userName = userName.value.text,
                                        password = password.value.text
                                        )
                                } else {
                                    userName.value = TextFieldValue("")
                                    password.value = TextFieldValue("")
                                    settingsPreferenceDataSource.resetRememberUser()
                                }
                                _userUiState.success(result.data)
                            } else {
                                settingsPreferenceDataSource.resetUser()
                            }
                        }
                    }
                }
        }
    }

    fun clearError() {
        _userUiState.success(null)
    }

    private suspend fun getUser() {
        //clearError()
        val user = settingsPreferenceDataSource.getUser().first()
        if (user.hasSession()) {
            _userUiState.success(user)
        } else {
            _userUiState.success(null)
        }
    }

    private suspend fun getRememberUser() {
        val rememberUser = settingsPreferenceDataSource.getRememberUser().first()
        if (rememberUser.userName.isNotEmpty() && rememberUser.password.isNotEmpty()) {
            serverUrl.value = TextFieldValue(rememberUser.serverUrl)
            userName.value = TextFieldValue(rememberUser.userName)
            password.value = TextFieldValue(rememberUser.password)
            rememberSession.value = true
        }
    }
}
