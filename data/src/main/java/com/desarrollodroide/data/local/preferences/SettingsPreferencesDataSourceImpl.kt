package com.desarrollodroide.data.local.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.copy
import com.desarrollodroide.data.mapper.toProtoEntity
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import com.desarrollodroide.network.model.SessionDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.desarrollodroide.data.RememberUserPreferences
import com.desarrollodroide.data.helpers.ThemeMode
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class SettingsPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val protoDataStore: DataStore<UserPreferences>,
    private val rememberUserProtoDataStore: DataStore<RememberUserPreferences>

) : SettingsPreferenceDataSource {

    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    // Use with stateIn
    override val userDataStream = protoDataStore.data
        .map {
            User(
                token = it.token,
                session = it.session,
                account = Account(
                    id = it.id,
                    userName = it.username,
                    owner = it.owner,
                    password = it.password,
                    serverUrl = it.url,
                    isLegacyApi = it.isLegacyApi
                )
            )
        }

    override fun getUser(): Flow<User> {
        return protoDataStore.data
            .catch {
                Log.v("Error!!!", it.message.toString())
            }
            .map { preference ->
                User(
                    token = preference.token,
                    session = preference.session,
                    account = Account(
                        id = preference.id,
                        userName = preference.username,
                        owner = preference.owner,
                        password = preference.password,
                        serverUrl = preference.url,
                        isLegacyApi = preference.isLegacyApi
                    )
                )
            }
    }

    override suspend fun saveUser(
        session: UserPreferences,
        serverUrl: String,
        password: String,
    ) {
        protoDataStore.updateData { protoSession ->
            protoSession.copy {
                this.id = session.id
                this.username = session.username
                this.password = password
                this.session = session.session
                this.url = serverUrl
                this.token = session.token
                this.isLegacyApi = session.isLegacyApi
            }
        }
    }

    override val rememberUserDataStream = rememberUserProtoDataStore.data
        .map {
            Account(
                id = it.id,
                userName = it.username,
                owner = false,
                password = it.password,
                serverUrl = it.url,
                isLegacyApi = false // Set a default value
            )
        }

    override fun getRememberUser(): Flow<Account> {
        return rememberUserProtoDataStore.data
            .catch {
                Log.v("Error!!!", it.message.toString())
            }
            .map { preference ->
                Account(
                    id = preference.id,
                    userName = preference.username,
                    owner = false,
                    password = preference.password,
                    serverUrl = preference.url,
                    isLegacyApi = false // Set a default value
                )
            }
    }

    override suspend fun saveRememberUser(
        url: String,
        userName: String,
        password: String,
    ) {
        rememberUserProtoDataStore.updateData { protoSession ->
            protoSession.copy {
                this.id = 1
                this.username = userName
                this.password = password
                this.url = url
            }
        }
    }

    override suspend fun getUrl(): String = getUser().first().account.serverUrl

    override suspend fun getSession(): String = getUser().first().session

    override suspend fun getToken(): String = getUser().first().token

    override suspend fun getIsLegacyApi(): Boolean = getUser().first().account.isLegacyApi

    override suspend fun resetUser() {
        saveUser(
            password = "",
            session = SessionDTO(null, null, null).toProtoEntity(),
            serverUrl = "",
        )
    }

    override suspend fun resetRememberUser() {
        saveRememberUser(
            url = "",
            userName = "",
            password = ""
        )
    }

    override fun setTheme(mode: ThemeMode) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[THEME_MODE_KEY] = mode.name
            }
        }
    }

    override fun getThemeMode(): ThemeMode {
        return runBlocking {
            val preferences = dataStore.data.firstOrNull()
            val modeName = preferences?.get(THEME_MODE_KEY) ?: ThemeMode.AUTO.name
            ThemeMode.valueOf(modeName)
        }
    }
    override suspend fun getMakeArchivePublic(): Boolean {
        return rememberUserProtoDataStore.data.map { it.makeArchivePublic }.first()
    }

    override suspend fun setMakeArchivePublic(newValue: Boolean) {
        rememberUserProtoDataStore.updateData { preferences ->
            preferences.toBuilder().setMakeArchivePublic(newValue).build()
        }
    }

    override suspend fun getCreateEbook(): Boolean {
        return rememberUserProtoDataStore.data.map { it.createEbook }.first()
    }

    override suspend fun setCreateEbook(newValue: Boolean) {
        rememberUserProtoDataStore.updateData { preferences ->
            preferences.toBuilder().setCreateEbook(newValue).build()
        }
    }

    override suspend fun getCreateArchive(): Boolean {
        return rememberUserProtoDataStore.data.map { it.createArchive }.first()
    }

    override suspend fun setCreateArchive(newValue: Boolean) {
        rememberUserProtoDataStore.updateData { preferences ->
            preferences.toBuilder().setCreateArchive(newValue).build()
        }
    }

}
