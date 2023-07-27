package com.shiori.data.repository

import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getUser(): User
    suspend fun getUserName(): Flow<String>

    val userDataStream: Flow<User>
}