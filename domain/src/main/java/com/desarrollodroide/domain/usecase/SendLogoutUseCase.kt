package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class SendLogoutUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
    ): Flow<Result<String?>> {
        return authRepository.sendLogout(
            serverUrl = serverUrl,
            xSession = xSession
        ).flowOn(Dispatchers.IO)
    }
}