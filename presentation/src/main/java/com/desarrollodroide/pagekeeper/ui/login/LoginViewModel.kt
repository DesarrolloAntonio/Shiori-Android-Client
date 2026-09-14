package com.desarrollodroide.pagekeeper.ui.login

import android.util.Log
import com.google.gson.JsonParser
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    // Kept in the saved state so a process death, e.g. while switching to a password manager, does
    // not empty the form. The password is deliberately not kept.
    var rememberSession = savedStateHandle.savedMutableState(KEY_REMEMBER, false)

    var serverUrl = savedStateHandle.savedMutableState(KEY_SERVER_URL, "")
    var userName = savedStateHandle.savedMutableState(KEY_USER_NAME, "")
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
                            val error = result.error.messageForUser()
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
                                // Publish it: this branch used to leave the spinner up for good.
                                _userUiState.error(errorMessage = "The server did not return a session")
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
                            } else {
                                // Every other failure has to end the spinner, not only an IO error:
                                // a 401 from a proxy or a 5xx used to fall through here and leave a
                                // dialog on screen that cannot be dismissed.
                                Log.v("LoginViewModel", "Error connecting to server")
                                val error = result.error.messageForUser()
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
                            val error = result.error.messageForUser()
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

/**
 * What the login screen tells the user about a failed request.
 *
 * An HTTP error carries the response body as its message. Shown as it came, that was
 * `{"ok":false,"message":"…"}` from Shiori, or a reverse proxy's whole HTML page. Shiori's own
 * message is used when the body is Shiori's JSON; any other body is never shown, only its status.
 */
internal fun Result.ErrorType?.messageForUser(): String {
    if (this is Result.ErrorType.HttpError) {
        val shioriMessage = runCatching {
            JsonParser.parseString(message.orEmpty()).asJsonObject.get("message")
                ?.takeIf { it.isJsonPrimitive }?.asString
        }.getOrNull()
        if (!shioriMessage.isNullOrBlank()) return shioriMessage
        statusCode?.let { return "The server answered HTTP $it" }
    }
    return this?.throwable?.message ?: this?.message ?: "Unknown error"
}

private const val KEY_SERVER_URL = "login_server_url"
private const val KEY_USER_NAME = "login_user_name"
private const val KEY_REMEMBER = "login_remember"

/** Compose state that starts from, and writes every change back to, this saved state entry. */
private fun <T> SavedStateHandle.savedMutableState(key: String, initial: T): MutableState<T> {
    val state = mutableStateOf(get<T>(key) ?: initial)
    return object : MutableState<T> {
        override var value: T
            get() = state.value
            set(newValue) {
                state.value = newValue
                this@savedMutableState[key] = newValue
            }

        override fun component1(): T = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}
