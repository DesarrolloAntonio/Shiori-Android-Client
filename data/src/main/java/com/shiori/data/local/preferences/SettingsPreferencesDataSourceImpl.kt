package com.shiori.data.local.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.shiori.data.UserPreferences
import com.shiori.data.copy
import com.shiori.data.mapper.toProtoEntity
import com.shiori.model.Account
import com.shiori.model.User
import com.shiori.network.model.SessionDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val protoDataStore: DataStore<UserPreferences>
): SettingsPreferenceDataSource  {

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
                    serverUrl = it.url
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
                        serverUrl = preference.url
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
    override suspend fun getUrl(): String = getUser().first().account.serverUrl

    override suspend fun getSession(): String = getUser().first().session

    override suspend fun resetUser() {
        saveUser(
            password = "",
            session = SessionDTO(null, null).toProtoEntity(),
            serverUrl = ""
        )
    }

}
