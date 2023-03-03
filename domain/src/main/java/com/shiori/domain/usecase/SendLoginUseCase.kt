package com.shiori.domain.usecase

import com.shiori.data.repository.MainRepository
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

class SendLoginUseCase(
    private val mainRepository: MainRepository,
) {
    operator fun invoke(
        username: String,
        password: String,
        serverUrl: String
    ): Flow<User?> {
        return mainRepository.sendLogin(username, password, serverUrl)
    }
}