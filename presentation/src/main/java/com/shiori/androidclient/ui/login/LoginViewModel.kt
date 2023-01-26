package com.shiori.androidclient.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiori.domain.model.User
import com.shiori.domain.repository.MainRepository
import com.shiori.domain.usecase.GetBookmarksUseCase
import kotlinx.coroutines.launch
import com.shiori.domain.model.state.Result

class LoginViewModel (
    private val mainRepository: MainRepository,
    private val useCase: GetBookmarksUseCase
): ViewModel() {

    var loading = mutableStateOf(false)
    var rememberSession = mutableStateOf(false)
    var userName = mutableStateOf(TextFieldValue())
    var password = mutableStateOf(TextFieldValue())
    var serverUrl = mutableStateOf(TextFieldValue())
    var loginError = mutableStateOf(false)
    var netWorkError = mutableStateOf(false)
    var user = mutableStateOf<User?>(null)

     suspend fun sendLogin(){

    }

    suspend fun getUser() {
        mainRepository.getUser().collect { user->
            this.user.value = user
        }
    }

    suspend fun saveUser(userName: String, password: String) {
        viewModelScope.launch {
            mainRepository.saveUser(userName, password)
        }
    }

    suspend fun getData() {
        useCase.execute(Unit)
            .collect { result ->
                when (result) {
                    is Result.Error -> {
                        loading.value = false
                        //error.value = true
                    }
                    is Result.Loading -> {
                        loading.value = true
                        //list = result.data
                    }
                    is Result.Success -> {
                        loading.value = false
                        //list = result.data
                    }
                }
            }
    }
}