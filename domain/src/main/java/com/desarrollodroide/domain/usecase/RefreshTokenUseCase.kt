package com.desarrollodroide.domain.usecase

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Trades the stored token for a new one with a fresh expiry.
 *
 * The server issues tokens with a 30 day life and never renews them on its own, so a client that
 * only ever logs in once eventually finds itself holding a dead token and discovers it through a
 * failed request. Refreshing on start keeps an active user signed in indefinitely.
 */
class RefreshTokenUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(
        serverUrl: String,
        token: String,
    ): Flow<Result<String?>> =
        authRepository.refreshToken(
            serverUrl = serverUrl,
            token = token,
        ).flowOn(Dispatchers.IO)
}
