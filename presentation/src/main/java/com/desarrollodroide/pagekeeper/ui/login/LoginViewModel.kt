package com.desarrollodroide.pagekeeper.ui.login

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.domain.usecase.RefreshTokenUseCase
import com.desarrollodroide.domain.usecase.SendLoginUseCase
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.domain.usecase.SystemLivenessUseCase
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.pagekeeper.ui.components.idle
import kotlinx.coroutines.delay

class LoginViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val loginUseCase: SendLoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val livenessUseCase: SystemLivenessUseCase,
) : ViewModel() {

    var rememberSession = mutableStateOf(false)

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

    private val _serverAvailabilityUiState = MutableStateFlow(UiState<LivenessResponse>(idle = true))
    val serverAvailabilityUiState = _serverAvailabilityUiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUser()
            getRememberUser()
        }
    }

    fun sendLogin() {
        viewModelScope.launch {
            loginUseCase.invoke(
                username = userName.value,
                password = password.value,
                serverUrl = serverUrl.value,
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
                                settingsPreferenceDataSource.resetData()
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
                        is Result.Error -> {
                            if (result.error?.statusCode == 404){
                                // Liveness not supported, versión < 1.6
                                sendLogin()
                                Log.v("LoginViewModel", "Liveness not supported")
                            } else if (result.error is Result.ErrorType.IOError) {
                                // Error connecting to server
                                Log.v("LoginViewModel", "Error connecting to server")
                                val error = result.error?.throwable?.message?:result.error?.message?:"Unknown error"
                                _livenessUiState.error(errorMessage = error)
                            }
                        }

                        is Result.Loading -> {
                            _livenessUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("LoginViewModel", "Liveness: ${result.data}")
                            settingsPreferenceDataSource.setServerVersion(result.data?.message?.version?:"")
                            _livenessUiState.success(result.data)
                            sendLogin()
                        }
                    }
                }
        }
    }

    fun checkServerAvailability(){
        viewModelScope.launch {
            livenessUseCase.invoke(serverUrl.value)
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v("LoginViewModel", "Server Availability error")
                            val error = result.error?.throwable?.message?:result.error?.message?:"Unknown error"
                            _serverAvailabilityUiState.error(errorMessage = error)
                        }
                        is Result.Loading -> {
                            _serverAvailabilityUiState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("LoginViewModel", "Server Availability: ${result.data}")
                            delay(1000)
                            _serverAvailabilityUiState.success(result.data)
                        }
                    }
                }
        }
    }

    fun clearState() {
        _userUiState.success(null)
        _livenessUiState.success(null)
    }

    private suspend fun getUser() {
        val user = settingsPreferenceDataSource.getUser().first()
        if (user.hasSession()) {
            _userUiState.success(user)
            renewSession(user)
        } else {
            _userUiState.success(null)
        }
    }

    /**
     * Renews the stored token in the background.
     *
     * The server issues tokens with a 30 day life and never renews them on its own, so a client
     * that logged in once eventually holds a dead token and only finds out through a failed
     * request. Refreshing on each start keeps an active user signed in indefinitely.
     *
     * Deliberately not awaited: the http client has a 30 second timeout, and blocking start up on
     * it would leave anyone whose server is unreachable staring at an empty screen before the app
     * they can use offline appears. A failure here changes nothing, the existing token is kept and
     * the normal request path reports an expired session if it really is expired.
     */
    private fun renewSession(user: User) {
        viewModelScope.launch {
            val serverUrl = settingsPreferenceDataSource.getUrl()
            if (serverUrl.isEmpty()) return@launch

            refreshTokenUseCase(serverUrl = serverUrl, token = user.token)
                .collect { result ->
                    if (result is Result.Error) {
                        Log.v("LoginViewModel", "Could not renew session: ${result.error?.message}")
                    }
                }
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

    fun resetServerAvailabilityUiState() {
        _serverAvailabilityUiState.idle(true)
    }
}
