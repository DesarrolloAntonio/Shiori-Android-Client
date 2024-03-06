package com.desarrollodroide.data.local.preferences

import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow

interface SettingsPreferenceDataSource {

    val userDataStream: Flow<User>
    fun getUser(): Flow<User>
    suspend fun saveUser(
        session: UserPreferences,
        serverUrl: String,
        password: String,
    )
    val rememberUserDataStream: Flow<Account>
    fun getRememberUser(): Flow<Account>
    suspend fun saveRememberUser(
        url: String,
        userName: String,
        password: String,
    )

    suspend fun getUrl(): String
    suspend fun getSession(): String
    suspend fun getToken(): String
    suspend fun resetUser()
    suspend fun resetRememberUser()
    fun setTheme(mode: ThemeMode)
    fun getThemeMode(): ThemeMode
    suspend fun getMakeArchivePublic(): Boolean
    suspend fun setMakeArchivePublic(newValue: Boolean)
    suspend fun getCreateEbook(): Boolean
    suspend fun setCreateEbook(newValue: Boolean)
    suspend fun getCreateArchive(): Boolean
    suspend fun setCreateArchive(newValue: Boolean)
    suspend fun getIsLegacyApi(): Boolean
}