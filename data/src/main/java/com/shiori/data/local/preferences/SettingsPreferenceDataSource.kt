package com.shiori.data.local.preferences

import com.shiori.data.UserPreferences
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

interface SettingsPreferenceDataSource {

    val userDataStream: Flow<User>
    fun getUser(): Flow<User>
    suspend fun saveUser(
        session: UserPreferences,
        serverUrl: String,
        password: String,
    )
    suspend fun getUrl(): String

    suspend fun getSession(): String
    suspend fun resetUser()
}