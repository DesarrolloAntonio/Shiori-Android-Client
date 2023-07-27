package com.shiori.domain.usecase

import com.shiori.data.repository.AuthRepository
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow
import com.shiori.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class SendLoginUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(
        username: String,
        password: String,
        serverUrl: String
    ): Flow<Result<User?>> {
        return authRepository.sendLogin(username, password, serverUrl).flowOn(Dispatchers.IO)
    }
}