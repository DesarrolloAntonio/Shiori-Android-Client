package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.AuthRepository
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class SendLoginUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(
        username: String,
        password: String,
        serverUrl: String,
        isLegacyApi: Boolean,
    ): Flow<Result<User?>> {
        return if (isLegacyApi)
            authRepository.sendLogin(username, password, serverUrl).flowOn(Dispatchers.IO)
        else
            authRepository.sendLoginV1(username, password, serverUrl).flowOn(Dispatchers.IO)
    }
}