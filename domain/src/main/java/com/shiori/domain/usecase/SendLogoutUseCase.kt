package com.shiori.domain.usecase

import com.shiori.data.repository.AuthRepository
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow
import com.shiori.common.result.Result
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