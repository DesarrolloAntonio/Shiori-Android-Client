package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.runtime.mutableStateOf
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
import com.desarrollodroide.domain.usecase.SystemLivenessUseCase
import com.desarrollodroide.model.LivenessResponse

class LoginViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val loginUseCase: SendLoginUseCase,
    private val livenessUseCase: SystemLivenessUseCase,
) : ViewModel() {

    var rememberSession = mutableStateOf(false)
//    var userName = mutableStateOf("testing")
//    var password = mutableStateOf("shiori")
//    var serverUrl = mutableStateOf("http://144.24.183.231:8086")

//    var userName = mutableStateOf("Test")
//    var password = mutableStateOf("Test")
//    var serverUrl = mutableStateOf("http://192.168.1.12:8080/")
//
    var serverUrl = mutableStateOf("")
    var userName = mutableStateOf("")
    var password = mutableStateOf("")

    val userNameError = mutableStateOf(false)
    val passwordError = mutableStateOf(false)
    val urlError = mutableStateOf(false)

    private val _userUiState = MutableStateFlow(UiState<User>(idle = true))
    val userUiState = _userUiState.asStateFlow()

    private val _livenessUiState = MutableStateFlow(UiState<LivenessResponse>(idle = true))
    val livenessUiState = _livenessUiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUser()
            getRememberUser()
            //checkSystemLiveness()
        }
    }

    fun sendLogin() {
        viewModelScope.launch {
            loginUseCase.invoke(
                username = userName.value,
                password = password.value,
                serverUrl = serverUrl.value
            )
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            val error = result.error?.throwable?.message?:result.error?.message?:"Unknown error"
                            _userUiState.error(
                                errorMessage = error
                            )
                        }

                        is Result.Loading -> {
                            _userUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            if (result.data != null && result.data?.hasSession() == true) {
                                if (rememberSession.value) {
                                    settingsPreferenceDataSource.saveRememberUser(
                                        url = serverUrl.value,
                                        userName = userName.value,
                                        password = password.value
                                        )
                                } else {
                                    userName.value = ""
                                    password.value = ""
                                    serverUrl.value = ""
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

    fun checkSystemLiveness(){
        viewModelScope.launch {
            livenessUseCase.invoke(serverUrl.value)
                .collect { result ->
                    when (result) {
                        is Result.Error -> {}

                        is Result.Loading -> {
//                            _userUiState.isLoading(true)
                        }

                        is Result.Success -> {
//                            if (result.data != null) {
//                                _userUiState.success(result.data)
//                            }
                        }
                    }
                }
        }
    }

    fun clearState() {
        _userUiState.success(null)
    }

    private suspend fun getUser() {
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
            serverUrl.value = rememberUser.serverUrl
            userName.value = rememberUser.userName
            password.value = rememberUser.password
            rememberSession.value = true
        }
    }
}
