package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.extensions.toJson
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.model.User
import com.desarrollodroide.network.model.LoginRequestPayload
import com.desarrollodroide.network.model.LoginResponseDTO
import com.desarrollodroide.network.model.SessionDTO
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class AuthRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val errorHandler: ErrorHandler
) : AuthRepository {

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
                serverUrl = serverUrl,
            )
        }
        override fun fetchFromLocal() = settingsPreferenceDataSource.getUser()

        override suspend fun fetchFromRemote() = apiService.sendLogin(
            "${serverUrl.removeTrailingSlash()}/api/login",
            LoginRequestPayload(
                username = username,
                password = password
            ).toJson()
        )

        override fun shouldFetch(data: User?) = true

    }.asFlow().flowOn(Dispatchers.IO)


    override fun sendLogout(
        serverUrl: String,
        xSession: String
    ) = object :
        NetworkBoundResource<String, String>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: String) {
            settingsPreferenceDataSource.resetData()
        }

        override fun fetchFromLocal() = flowOf("")

        override suspend fun fetchFromRemote() = apiService.sendLogout(
            xSessionId = xSession,
            url = "${serverUrl.removeTrailingSlash()}/api/logout")

        override fun shouldFetch(data: String?) = true

    }.asFlow().flowOn(Dispatchers.IO)

    override fun sendLoginV1(
        username: String,
        password: String,
        serverUrl: String
    ) = object :
        NetworkBoundResource<LoginResponseDTO, User>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: LoginResponseDTO) {
            settingsPreferenceDataSource.saveUser(
                password = password,
                session = response.toProtoEntity(username),
                serverUrl = serverUrl,
            )
        }
        override fun fetchFromLocal() = settingsPreferenceDataSource.getUser()

        override suspend fun fetchFromRemote() = apiService.sendLoginV1(
            "${serverUrl.removeTrailingSlash()}/api/v1/auth/login",
            LoginRequestPayload(
                username = username,
                password = password
            ).toJson()
        )

        override fun shouldFetch(data: User?) = true

    }.asFlow().flowOn(Dispatchers.IO)

}