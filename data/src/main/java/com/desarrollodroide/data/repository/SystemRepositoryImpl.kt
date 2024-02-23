package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class SystemRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val errorHandler: ErrorHandler
) : SystemRepository {
    override fun liveness(
        serverUrl: String,
    ) = object :
        NetworkBoundResource<String, String>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: String) {

        }

        override fun fetchFromLocal() = flowOf("")

        override suspend fun fetchFromRemote() = apiService.systemLiveness(
            url = "${serverUrl.removeTrailingSlash()}/system/liveness"
        )

        override fun shouldFetch(data: String?) = true

    }.asFlow().flowOn(Dispatchers.IO)

}