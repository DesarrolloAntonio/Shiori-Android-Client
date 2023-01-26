package com.shiori.data.local.preferences

import com.shiori.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserPreference {

    fun userName(): Flow<String>
    suspend fun saveUserName(name: String)
    suspend fun user(): Flow<User>
    suspend fun saveUser(user: User)
    suspend fun saveUrl(url: String)
    suspend fun saveSession(session: String)

}