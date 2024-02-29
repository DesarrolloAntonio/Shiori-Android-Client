package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.network.model.LivenessResponseDTO
import com.desarrollodroide.network.retrofit.NetworkNoCacheResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SystemRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val errorHandler: ErrorHandler
) : SystemRepository {
    override fun liveness(
        serverUrl: String,
    ) = object :
        NetworkNoCacheResource<LivenessResponseDTO, LivenessResponse?>(errorHandler = errorHandler) {

        override suspend fun fetchFromRemote() = apiService.systemLiveness(
            url = "${serverUrl.removeTrailingSlash()}/system/liveness"
        )

        override fun fetchResult(data: LivenessResponseDTO): Flow<LivenessResponse?> {
            return flow {
                data?.let {
                    emit(it.toDomainModel())
                }
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)

}