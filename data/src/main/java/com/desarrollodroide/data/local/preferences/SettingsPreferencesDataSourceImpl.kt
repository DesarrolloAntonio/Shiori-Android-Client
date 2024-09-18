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
import com.desarrollodroide.data.HideTag
import com.desarrollodroide.data.RememberUserPreferences
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class SettingsPreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val protoDataStore: DataStore<UserPreferences>,
    private val rememberUserProtoDataStore: DataStore<RememberUserPreferences>,
    private val hideTagDataStore: DataStore<HideTag>,

    ) : SettingsPreferenceDataSource {

    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    val COMPACT_VIEW = booleanPreferencesKey("compact_view")
    val AUTO_ADD_BOOKMARK = booleanPreferencesKey("auto_add_bookmark")
    val CATEGORIES_VISIBLE = booleanPreferencesKey("categories_visible")
    val SELECTED_CATEGORIES_KEY = stringPreferencesKey("selected_categories")
    val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")

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

    override suspend fun setCompactView(isCompactView: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[COMPACT_VIEW] = isCompactView
            }
        }
    }

    override val compactViewFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[COMPACT_VIEW] ?: false
        }

    override suspend fun setCategoriesVisible(isCategoriesVisible: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[CATEGORIES_VISIBLE] = isCategoriesVisible
            }
        }
    }
    override suspend fun getCategoriesVisible(): Boolean = runBlocking {
        dataStore.data.firstOrNull()?.get(CATEGORIES_VISIBLE) ?: false
    }

    override val makeArchivePublicFlow: Flow<Boolean> = rememberUserProtoDataStore.data
        .map { it.makeArchivePublic }

    override suspend fun setMakeArchivePublic(newValue: Boolean) {
        rememberUserProtoDataStore.updateData { preferences ->
            preferences.toBuilder().setMakeArchivePublic(newValue).build()
        }
    }

    override val createEbookFlow: Flow<Boolean> = rememberUserProtoDataStore.data
        .map { it.createEbook }

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

    override suspend fun setSelectedCategories(categories: List<String>) {
        dataStore.edit { preferences ->
            val categoriesString = categories.joinToString(",")
            preferences[SELECTED_CATEGORIES_KEY] = categoriesString
        }
    }

    override suspend fun getSelectedCategories(): List<String> = runBlocking {
        dataStore.data.firstOrNull()?.get(SELECTED_CATEGORIES_KEY)?.split(",") ?: emptyList()
    }


    override  fun getUseDynamicColors(): Boolean = runBlocking {
        dataStore.data.firstOrNull()?.get(USE_DYNAMIC_COLORS) ?: false
    }

    override fun setUseDynamicColors(newValue: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[USE_DYNAMIC_COLORS] = newValue
            }
        }
    }

    override suspend fun setHideTag(tag: Tag?) {
        hideTagDataStore.updateData { currentHideTag ->
            when (tag) {
                null -> HideTag.getDefaultInstance()
                else -> currentHideTag.toBuilder()
                    .setId(tag.id)
                    .setName(tag.name)
                    .build()
            }
        }
    }

    override suspend fun getHideTag(): Tag? {
        return hideTagDataStore.data.first().let { hideTag ->
            if (hideTag == HideTag.getDefaultInstance()) null
            else Tag(id = hideTag.id, name = hideTag.name, selected = false, nBookmarks = 0)
        }
    }

    override suspend fun setAutoAddBookmark(isAutoAddBookmark: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_ADD_BOOKMARK] = isAutoAddBookmark
        }
    }
    override suspend fun getAutoAddBookmark(): Boolean = runBlocking {
        dataStore.data.firstOrNull()?.get(AUTO_ADD_BOOKMARK) ?: false
    }

}
