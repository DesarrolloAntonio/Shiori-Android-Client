package com.shiori.data.repository

import com.shiori.data.local.preferences.UserPreference
import com.shiori.data.remote.ApiService
import com.shiori.domain.model.Bookmark
import com.shiori.domain.model.User
import com.shiori.domain.model.state.Result
import com.shiori.domain.repository.MainRepository
import kotlinx.coroutines.flow.*

class MainRepositoryImpl(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) : MainRepository {
    override fun getBookmarks(): Flow<Result<out List<Bookmark>?>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveUser(userName: String, password: String){
        userPreference.saveUser(User(userName, password))
    }

    override suspend fun getUser(): Flow<User> = userPreference.user()

    override suspend fun storeUrl(url: String){

    }

    override suspend fun storeUser(user: User){

    }


}