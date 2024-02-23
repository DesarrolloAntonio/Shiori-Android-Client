package com.desarrollodroide.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.SystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class SystemLivenessUseCase(
    private val systemRepository: SystemRepository,
) {
    operator fun invoke(
        serverUrl: String
    ): Flow<Result<String?>> {
        return systemRepository.liveness(serverUrl).flowOn(Dispatchers.IO)
    }
}