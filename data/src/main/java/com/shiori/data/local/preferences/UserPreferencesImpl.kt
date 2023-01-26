package com.shiori.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.shiori.data.local.preferences.KEYS.KEY_PASSWORD
import com.shiori.data.local.preferences.KEYS.KEY_SERVER_URL
import com.shiori.data.local.preferences.KEYS.KEY_SESSION
import com.shiori.data.local.preferences.KEYS.KEY_USER_NAME
import com.shiori.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreference {

    override fun userName(): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preference ->
                preference[KEY_USER_NAME] ?: ""
            }
    }

    override suspend fun saveUserName(name: String) {
        dataStore.edit { preference ->
            preference[KEY_USER_NAME] = name
        }
    }

    override suspend fun user(): Flow<User> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preference ->
                val userName = preference[KEY_USER_NAME] ?: ""
                val passWord = preference[KEY_PASSWORD] ?: ""
                User(userName, passWord)
            }
    }

    override suspend fun saveUser(user: User) {
        dataStore.edit { preference ->
            preference[KEY_USER_NAME] = user.userName
            preference[KEY_PASSWORD] = user.password
        }
    }

    override suspend fun saveUrl(url: String) {
        dataStore.edit { preference ->
            preference[KEY_SERVER_URL] = url
        }
    }

    override suspend fun saveSession(session: String) {
        dataStore.edit { preference ->
            preference[KEY_SESSION] = session
        }
    }
}


object KEYS {
    val KEY_USER_NAME = stringPreferencesKey("user_name")
    val KEY_SERVER_URL = stringPreferencesKey("server_url")
    val KEY_PASSWORD = stringPreferencesKey("password")
    val KEY_SESSION = stringPreferencesKey("session")
}