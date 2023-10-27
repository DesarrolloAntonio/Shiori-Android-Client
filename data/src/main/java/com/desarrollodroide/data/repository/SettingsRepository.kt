package com.desarrollodroide.data.repository

import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getUser(): User
    suspend fun getUserName(): Flow<String>

    val userDataStream: Flow<User>
    suspend fun setTheme(isDark: Boolean)
    fun isDarkTheme(): Boolean
}