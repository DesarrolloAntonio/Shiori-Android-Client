package com.shiori.data.local.preferences

import com.shiori.data.UserPreferences
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

interface UserPreferenceDataSource {

    val userDataStream: Flow<User>
    suspend fun getUser(): Flow<User>
    suspend fun saveUser(session: UserPreferences)
    suspend fun saveUrl(url: String)

}