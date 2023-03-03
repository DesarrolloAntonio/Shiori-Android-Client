package com.shiori.data.local.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.shiori.data.UserPreferences
import com.shiori.data.copy
import com.shiori.model.Account
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val protoDataStore: DataStore<UserPreferences>
): UserPreferenceDataSource  {

    // Para usar con stateIn
    override val userDataStream = protoDataStore.data
        .map {
            User(
                session = it.session,
                account = Account(
                    id = it.id,
                    userName = it.username,
                    owner = it.owner,
                    password = it.password
                )
            )
        }

    override suspend fun getUser(): Flow<User> {
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
                        password = preference.password
                    )
                )
            }
    }

    override suspend fun saveUser(session: UserPreferences) {
        protoDataStore.updateData { protoSession ->
            protoSession.copy {
                this.id = session.id
                this.username = session.username
                this.password = session.password
                this.session = session.session
            }
        }
    }

    override suspend fun saveUrl(url: String) {

    }
}
