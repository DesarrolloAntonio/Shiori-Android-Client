package com.shiori.data.repository

import android.util.Log
import com.shiori.data.extensions.toJson
import com.shiori.data.local.preferences.UserPreferenceDataSource
import com.shiori.data.mapper.*
import com.shiori.model.Account
import com.shiori.model.Bookmark
import com.shiori.model.User
import com.shiori.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MainRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val userPreferenceDataSource: UserPreferenceDataSource
) : MainRepository {
    override fun getBookmarks(): Flow<List<Bookmark>> {
        return flow {
//            emit(Result.Loading(null))
            val result = apiService.getBookmarks("http://144.24.174.227:49153/api/bookmarks")
            if (result.isSuccessful && result.body() != null) {
                emit(result.body()!!.map { it.toDomainModel() })
            } else {
                emit(emptyList())
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun sendLogin(
        username: String,
        password: String,
        serverUrl: String
    ): Flow<User?> {
        return flow {
            val account = Account(id = -1, userName = username, password = password, owner = false)
            Log.v("account**", account.toJson())
            val result =
                apiService.sendLogin("$serverUrl/api/login", account.toRequestBody().toJson())
            if (result.isSuccessful) {
                val sessionDTO = result.body()
                if (sessionDTO != null) {
                    if (sessionDTO.session?.isEmpty() == true) {
                        emit(null)
                    } else {
                        userPreferenceDataSource.saveUser(sessionDTO.toProtoEntityWith(password))
                        emit(sessionDTO.toDomainModelWith(account.password))
                    }
                } else {
                }
            } else {
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }
}