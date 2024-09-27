package com.desarrollodroide.data.local.preferences

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.preferencesOf
import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.helpers.ThemeMode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import androidx.datastore.preferences.core.stringPreferencesKey
import com.desarrollodroide.data.HideTag
import com.desarrollodroide.data.RememberUserPreferences
import com.desarrollodroide.data.SystemPreferences
import kotlinx.coroutines.flow.first
import app.cash.turbine.test
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.Flow
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class SettingsPreferencesDataSourceImplTest {

    private lateinit var settingsPreferencesDataSourceImpl: SettingsPreferencesDataSourceImpl
    private var preferencesDataStore: DataStore<Preferences> = mock()
    private val protoDataStoreMock: DataStore<UserPreferences> = mock()
    private val systemPreferencesDataStoreMock: DataStore<SystemPreferences> = mock()
    private val hideTagDataStoreMock: DataStore<HideTag> = mock()
    private val rememberUserProtoDataStoreMock: DataStore<RememberUserPreferences> = mock()

    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val CATEGORIES_VISIBLE_KEY = booleanPreferencesKey("categories_visible")
    private val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")

    @BeforeEach
    fun setUp() {
        settingsPreferencesDataSourceImpl = SettingsPreferencesDataSourceImpl(
            dataStore = preferencesDataStore,
            protoDataStore = protoDataStoreMock,
            systemPreferences = systemPreferencesDataStoreMock,
            rememberUserProtoDataStore = rememberUserProtoDataStoreMock,
            hideTagDataStore = hideTagDataStoreMock
        )
    }

    // --- Theme Mode Tests ---

    @Test
    fun `getThemeMode returns expected value`() = runTest {
        val expectedThemeMode = ThemeMode.LIGHT
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(THEME_MODE_KEY to expectedThemeMode.name)))
        val actualThemeMode = settingsPreferencesDataSourceImpl.getThemeMode()
        assertEquals(expectedThemeMode, actualThemeMode)
    }

    @Test
    fun `setThemeMode updates theme mode to DARK`() = runTest {
        val themeMode = ThemeMode.DARK
        settingsPreferencesDataSourceImpl.setTheme(themeMode)
        verifyPreferenceEdit(preferencesDataStore, THEME_MODE_KEY, themeMode.name)
    }

    @Test
    fun `getThemeMode retrieves persisted theme mode correctly after app restart`() = runTest {
        val expectedThemeMode = ThemeMode.DARK
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(THEME_MODE_KEY to expectedThemeMode.name)))
        val actualThemeMode = settingsPreferencesDataSourceImpl.getThemeMode()
        assertEquals(expectedThemeMode, actualThemeMode)
    }

    @Test
    fun `getThemeMode returns default theme mode when none is set`() = runTest {
        val defaultThemeMode = ThemeMode.AUTO
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf()))
        val actualThemeMode = settingsPreferencesDataSourceImpl.getThemeMode()
        assertEquals(defaultThemeMode, actualThemeMode)
    }

    // --- Categories Visibility Tests ---

    @Test
    fun `getCategoriesVisible returns expected value`() = runTest {
        val expectedValue = true
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(CATEGORIES_VISIBLE_KEY to expectedValue)))
        val actualValue = settingsPreferencesDataSourceImpl.getCategoriesVisible()
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `setCategoriesVisible updates categories visible to false`() = runTest {
        val categoriesVisible = false
        settingsPreferencesDataSourceImpl.setCategoriesVisible(categoriesVisible)
        verifyPreferenceEdit(preferencesDataStore, CATEGORIES_VISIBLE_KEY, categoriesVisible)
    }

    // --- Selected Categories Tests ---

    @Test
    fun `setSelectedCategories updates selected categories correctly`() = runTest {
        val selectedCategories = listOf("1", "2", "3")
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        settingsPreferencesDataSourceImpl.setSelectedCategories(selectedCategories)

        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(selectedCategories, updatedPreferences.selectedCategoriesList)
    }

    @Test
    fun `addSelectedCategory adds category correctly`() = runTest {
        val newTag = Tag(id = 4, name = "New Category", selected = false, nBookmarks = 0)
        val existingCategories = listOf("1", "2", "3")
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        val initialPreferences = SystemPreferences.newBuilder()
            .addAllSelectedCategories(existingCategories)
            .build()

        settingsPreferencesDataSourceImpl.addSelectedCategory(newTag)

        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val updatedPreferences = captor.firstValue.invoke(initialPreferences)
        assertEquals(existingCategories + "4", updatedPreferences.selectedCategoriesList)
    }

    @Test
    fun `removeSelectedCategory removes category correctly`() = runTest {
        val tagToRemove = Tag(id = 2, name = "Category to Remove", selected = false, nBookmarks = 0)
        val existingCategories = listOf("1", "2", "3")
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        val initialPreferences = SystemPreferences.newBuilder()
            .addAllSelectedCategories(existingCategories)
            .build()

        settingsPreferencesDataSourceImpl.removeSelectedCategory(tagToRemove)

        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val updatedPreferences = captor.firstValue.invoke(initialPreferences)
        assertEquals(listOf("1", "3"), updatedPreferences.selectedCategoriesList)
    }

    // --- UserPreferences Tests ---

    @Test
    fun `getUser returns User with correct data`() = runTest {
        val expectedUser = UserPreferences.newBuilder()
            .setId(1)
            .setUsername("testUser")
            .setSession("session123")
            .setToken("tokenABC")
            .build()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(expectedUser))
        val actualUser = settingsPreferencesDataSourceImpl.getUser().first()
        assertEquals(expectedUser.username, actualUser.account.userName)
        assertEquals(expectedUser.session, actualUser.session)
        assertEquals(expectedUser.token, actualUser.token)
    }

    @Test
    fun `saveUser updates UserPreferences correctly`() = runTest {
        val userPreferences = UserPreferences.newBuilder().setId(1).build()
        val serverUrl = "https://example.com"
        val password = "password123"
        settingsPreferencesDataSourceImpl.saveUser(userPreferences, serverUrl, password)
        verify(protoDataStoreMock).updateData(any())
    }

    @Test
    fun `resetUser resets user data correctly`() = runTest {
        settingsPreferencesDataSourceImpl.resetUser()
        verify(protoDataStoreMock).updateData(any())
    }

    // --- RememberUserPreferences Tests ---

    @Test
    fun `resetRememberUser resets remembered user data correctly`() = runTest {
        settingsPreferencesDataSourceImpl.resetRememberUser()
        verify(rememberUserProtoDataStoreMock).updateData(any())
    }

    @Test
    fun `getRememberUser returns Account with correct data`() = runTest {
        val expectedAccount = RememberUserPreferences.newBuilder()
            .setId(1)
            .setUsername("rememberUser")
            .setPassword("password123")
            .setUrl("https://example-remember.com")
            .build()
        whenever(rememberUserProtoDataStoreMock.data).thenReturn(flowOf(expectedAccount))
        val actualAccount = settingsPreferencesDataSourceImpl.getRememberUser().first()
        assertEquals(expectedAccount.username, actualAccount.userName)
        assertEquals(expectedAccount.url, actualAccount.serverUrl)
        assertEquals(expectedAccount.password, actualAccount.password)
    }

    @Test
    fun `saveRememberUser updates RememberUserPreferences correctly`() = runTest {
        val url = "https://example-save.com"
        val userName = "saveUser"
        val password = "savePass123"
        settingsPreferencesDataSourceImpl.saveRememberUser(url, userName, password)
        verify(rememberUserProtoDataStoreMock).updateData(any())
    }

    // --- System Preferences Tests ---

    @Test
    fun `setMakeArchivePublic updates preference correctly`() = runTest {
        val newValue = true
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()
        settingsPreferencesDataSourceImpl.setMakeArchivePublic(newValue)
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(newValue, updatedPreferences.makeArchivePublic)
    }

    @Test
    fun `setCreateEbook updates preference correctly`() = runTest {
        val newValue = true
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()
        settingsPreferencesDataSourceImpl.setCreateEbook(newValue)
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(newValue, updatedPreferences.createEbook)
    }

    // --- Flow Tests ---

    @Test
    fun `compactViewFlow emits correct value`() = runTest {
        val mockSystemPreferences = SystemPreferences.newBuilder()
            .setCompactView(true)
            .build()
        val mockSystemPreferencesFlow: Flow<SystemPreferences> = flowOf(mockSystemPreferences)
        `when`(systemPreferencesDataStoreMock.data).thenReturn(mockSystemPreferencesFlow)

        settingsPreferencesDataSourceImpl.compactViewFlow.test {
            val emittedItem = awaitItem()
            assertEquals(true, emittedItem)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `makeArchivePublicFlow emits correct value`() = runTest {
        val mockSystemPreferences = SystemPreferences.newBuilder()
            .setMakeArchivePublic(true)
            .build()
        val mockSystemPreferencesFlow: Flow<SystemPreferences> = flowOf(mockSystemPreferences)
        `when`(systemPreferencesDataStoreMock.data).thenReturn(mockSystemPreferencesFlow)

        settingsPreferencesDataSourceImpl.makeArchivePublicFlow.test {
            val emittedItem = awaitItem()
            assertEquals(true, emittedItem)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createEbookFlow emits correct value`() = runTest {
        val mockSystemPreferences = SystemPreferences.newBuilder()
            .setCreateEbook(true)
            .build()
        val mockSystemPreferencesFlow: Flow<SystemPreferences> = flowOf(mockSystemPreferences)
        `when`(systemPreferencesDataStoreMock.data).thenReturn(mockSystemPreferencesFlow)

        settingsPreferencesDataSourceImpl.createEbookFlow.test {
            val emittedItem = awaitItem()
            assertEquals(true, emittedItem)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private suspend fun <T> verifyPreferenceEdit(
    preferencesDataStore: DataStore<Preferences>,
    key: Preferences.Key<T>,
    expectedValue: T
) {
    val argumentCaptor = argumentCaptor<suspend (Preferences) -> Preferences>()
    verify(preferencesDataStore).updateData(argumentCaptor.capture())

    val preferences = mutablePreferencesOf()
    val updatedPreferences = argumentCaptor.firstValue(preferences)
    assertEquals(expectedValue, updatedPreferences[key])
}