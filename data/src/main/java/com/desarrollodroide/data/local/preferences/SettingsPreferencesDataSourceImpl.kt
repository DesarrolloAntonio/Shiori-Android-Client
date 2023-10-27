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
import kotlinx.coroutines.runBlocking

class SettingsPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val protoDataStore: DataStore<UserPreferences>,
    private val rememberUserProtoDataStore: DataStore<RememberUserPreferences>

) : SettingsPreferenceDataSource {

    val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    // Use with stateIn
    override val userDataStream = protoDataStore.data
        .map {
            User(
                session = it.session,
                account = Account(
                    id = it.id,
                    userName = it.username,
                    owner = it.owner,
                    password = it.password,
                    serverUrl = it.url,
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
                    session = preference.session,
                    account = Account(
                        id = preference.id,
                        userName = preference.username,
                        owner = preference.owner,
                        password = preference.password,
                        serverUrl = preference.url,
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
                )
            }
    }

    override suspend fun saveRememberUser(
        url: String,
        userName: String,
        password: String
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

    override suspend fun resetUser() {
        saveUser(
            password = "",
            session = SessionDTO(null, null).toProtoEntity(),
            serverUrl = ""
        )
    }

    override suspend fun resetRememberUser() {
        saveRememberUser(
            url = "",
            userName = "",
            password = ""
        )
    }

    override fun setTheme(isDark: Boolean) {
        return runBlocking {
            dataStore.edit { preferences ->
                preferences[DARK_THEME_KEY] = isDark
            }
        }
    }

    override fun isDarkTheme(): Boolean {
        return runBlocking {
            val preferences = dataStore.data.first()
            preferences[DARK_THEME_KEY] ?: false
        }
    }
}
