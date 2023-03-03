package com.shiori.androidclient.ui.login

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.data.repository.UserRepository
import com.shiori.data.result.asResult
import com.shiori.domain.usecase.SendLoginUseCase
import com.shiori.data.result.Result
import com.shiori.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val loginUseCase: SendLoginUseCase,
) : ViewModel() {

    var loading = mutableStateOf(false)
    var rememberSession = mutableStateOf(false)
    var userName = mutableStateOf(TextFieldValue("Memnoch"))
    var password = mutableStateOf(TextFieldValue("e%dPd3&eAV@#v7TKP%NvmZ5"))
    var serverUrl = mutableStateOf(TextFieldValue("http://144.24.174.227:49153/"))
    var loginError = mutableStateOf(false)
    var netWorkError = mutableStateOf(false)

    //    var session = mutableStateOf<Session?>(null)
    val userNameError = mutableStateOf(false)
    val passwordError = mutableStateOf(false)
    val urlError = mutableStateOf(false)

    var uiState: MutableStateFlow<LoginUiState> = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

//    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userDataStream.map {
//        Success(it)
//    }.stateIn(
//        scope = viewModelScope,
//        initialValue = Loading,
//        started = SharingStarted.WhileSubscribed(5_000)
//    )

    init {
        viewModelScope.launch {
            //saveUser("pepe", "123456")
            val user = getUser()
        }
    }

     fun sendLogin() {
         viewModelScope.launch {
             if (!userNameError.value && !passwordError.value && !urlError.value) {
                 loginUseCase.invoke(
                     username = userName.value.text,
                     password = password.value.text,
                     serverUrl = serverUrl.value.text
                 ).asResult()
                     .collect(){ session->
                         uiState.update {
                             when(session){
                                 is Result.Error -> {
                                     LoginUiState.Error
                                 }
                                 is Result.Loading -> {
                                     LoginUiState.Loading
                                 }
                                 is Result.Success -> {
                                     if (session.data != null){
                                         LoginUiState.Success(session.data!!)
                                     } else {
                                         LoginUiState.Error
                                     }
                                 }
                             }
                         }
                 }
             }
         }
    }

    private fun authorUiStateStream() {
        //val loginStream: Flow<UserSession> = loginUseCase.invoke()
    }

    suspend fun getUser() {
        userRepository.getUser().collect { user->
            Log.v("22", "2222")
            Log.v("user", user.session.toString())//this.user.value = user
            Log.v("user", user.account.userName)//this.user.value = user
            Log.v("user", user.account.password)//this.user.value = user
            Log.v("user", user.account.owner.toString())//this.user.value = user

        }
    }

    suspend fun saveUser(userName: String, password: String) {
        viewModelScope.launch {
            //userRepository.saveUser("Pepe")
        }
    }

//    suspend fun getBookmarks() {
//        getBookmarksUseCase.execute(Unit)
//            .collect { result ->
//                when (result) {
////                    is Result.Error -> {
////                        loading.value = false
////                        //error.value = true
////                    }
////                    is Result.Loading -> {
////                        loading.value = true
////                        //list = result.data
////                    }
////                    is Result.Success -> {
////                        loading.value = false
////                        //list = result.data
////                    }
//                }
//            }
//    }
}

sealed interface LoginUiState {
    object Loading : LoginUiState
    data class Success(val userSession: User) : LoginUiState
    object Error : LoginUiState
    object Idle : LoginUiState
}
