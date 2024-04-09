package com.desarrollodroide.data.repository

import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource
): SettingsRepository {
    override suspend fun getUser() = settingsPreferenceDataSource.userDataStream.map {
        User(
            token = it.token,
            session = it.session,
            account = Account(
                id = it.account.id,
                userName = it.account.userName,
                password = it.account.password,
                owner = it.account.owner,
                serverUrl = it.account.serverUrl,
                isLegacyApi = it.account.isLegacyApi
            )
        )
    }.first()

    override suspend fun getUserName() = settingsPreferenceDataSource.userDataStream.map { it.account.userName }

    override val userDataStream: Flow<User> =
        settingsPreferenceDataSource.userDataStream

    override suspend fun setThemeMode(themeMode: ThemeMode) {
       settingsPreferenceDataSource.setTheme(themeMode)
    }
    override fun getThemeMode() = settingsPreferenceDataSource.getThemeMode()

    override fun getUseDynamicColors() = settingsPreferenceDataSource.getUseDynamicColors()
    override suspend fun setUseDynamicColors(useDynamicColors: Boolean) {
        settingsPreferenceDataSource.setUseDynamicColors(useDynamicColors)
    }

}