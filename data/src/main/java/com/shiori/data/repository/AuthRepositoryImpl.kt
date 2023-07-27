package com.shiori.data.repository

import com.shiori.common.result.ErrorHandler
import com.shiori.data.helpers.toJson
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.data.mapper.*
import com.shiori.model.Account
import com.shiori.model.User
import com.shiori.network.model.LoginBodyContent
import com.shiori.network.model.SessionDTO
import com.shiori.network.retrofit.NetworkBoundResource
import com.shiori.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class AuthRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val errorHandler: ErrorHandler
) : AuthRepository {

//    override fun sendLogin(
//        username: String,
//        password: String,
//        serverUrl: String
//    ): Flow<User?> {
//        return flow {
//            try {
//                val account =
//                    Account(id = -1, userName = username, password = password, owner = false)
//                Log.v("account**", account.toJson())
//                val result = apiService.sendLogin("$serverUrl/api/login", account.toRequestBody().toJson())
//                val sessionDTO = result.body()
//                if (result.isSuccessful && sessionDTO != null && sessionDTO.session?.isNotEmpty() == true) {
//                    settingsPreferenceDataSource.saveUser(
//                        password = password,
//                        session = sessionDTO.toProtoEntity(),
//                        serverUrl = serverUrl
//                    )
//                    emit(sessionDTO.toDomainModel())
//                } else {
//                    emit(User(error = "Session is empty"))
//                }
//            } catch (exception: Exception) {
//                Log.v("Error!!", exception.message ?: "")
//                emit(User(error = exception.message ?: ""))
//            }
//        }.flowOn(Dispatchers.IO)
//    }


    override fun sendLogin(
        username: String,
        password: String,
        serverUrl: String
    ) = object :
        NetworkBoundResource<SessionDTO, User>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: SessionDTO) {
            settingsPreferenceDataSource.saveUser(
                password = password,
                session = response.toProtoEntity(),
                serverUrl = serverUrl
            )
        }
        override fun fetchFromLocal() = settingsPreferenceDataSource.getUser()

        override suspend fun fetchFromRemote() = apiService.sendLogin(
            "$serverUrl/api/login",
            LoginBodyContent(
                username = username,
                password = password
            ).toJson()
//            Account(
//                id = -1,
//                userName = username,
//                password = password,
//                owner = false
//            ).toRequestBody().toJson()
        )

        override fun shouldFetch(data: User?) = true

    }.asFlow().flowOn(Dispatchers.IO)


    override fun sendLogout(
        serverUrl: String,
        xSession: String
    ) = object :
        NetworkBoundResource<String, String>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: String) {
            settingsPreferenceDataSource.resetUser()
        }

        override fun fetchFromLocal() = flowOf("")

        override suspend fun fetchFromRemote() = apiService.sendLogout(
            xSessionId = xSession,
            url = "$serverUrl/api/logout")

        override fun shouldFetch(data: String?) = true

    }.asFlow().flowOn(Dispatchers.IO)
}