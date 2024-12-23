package com.desarrollodroide.data.local.preferences

import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow

interface SettingsPreferenceDataSource {

    val userDataStream: Flow<User>
    val compactViewFlow: Flow<Boolean>
    val makeArchivePublicFlow: Flow<Boolean>
    val createEbookFlow: Flow<Boolean>
    val autoAddBookmarkFlow: Flow<Boolean>
    val createArchiveFlow: Flow<Boolean>
    val hideTagFlow: Flow<Tag?>
    val selectedCategoriesFlow: Flow<List<String>>

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
    suspend fun resetData()
    suspend fun resetRememberUser()
    fun setTheme(mode: ThemeMode)
    fun getThemeMode(): ThemeMode
    suspend fun setMakeArchivePublic(newValue: Boolean)
    suspend fun setCreateEbook(newValue: Boolean)
    suspend fun setCreateArchive(newValue: Boolean)
    suspend fun setCompactView(isCompactView: Boolean)
    suspend fun setAutoAddBookmark(isAutoAddBookmark: Boolean)
    suspend fun getCategoriesVisible(): Boolean
    suspend fun setCategoriesVisible(isCategoriesVisible: Boolean)
    suspend fun setSelectedCategories(categories: List<String>)
    fun getUseDynamicColors(): Boolean
    fun setUseDynamicColors(newValue: Boolean)
    suspend fun setHideTag(tag: Tag?)
    suspend fun addSelectedCategory(tag: Tag)
    suspend fun removeSelectedCategory(tag: Tag)
    suspend fun getLastSyncTimestamp(): Long
    suspend fun setLastSyncTimestamp(timestamp: Long)
    suspend fun setCurrentTimeStamp()
    suspend fun getServerVersion(): String
    suspend fun setServerVersion(version: String)
    suspend fun getLastCrashLog(): String
    suspend fun setLastCrashLog(crash: String)
    suspend fun clearLastCrashLog()
}