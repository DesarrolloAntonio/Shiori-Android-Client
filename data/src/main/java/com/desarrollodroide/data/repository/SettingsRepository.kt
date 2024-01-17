package com.desarrollodroide.data.repository

import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getUser(): User
    suspend fun getUserName(): Flow<String>

    val userDataStream: Flow<User>
    fun getThemeMode(): ThemeMode
    suspend fun setThemeMode(themeMode: ThemeMode)
}