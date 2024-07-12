package com.desarrollodroide.data.local.preferences

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import com.desarrollodroide.data.RememberUserPreferences
import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.helpers.ThemeMode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import androidx.datastore.preferences.core.stringPreferencesKey
import com.desarrollodroide.data.HideTag
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class SettingsPreferencesDataSourceImplTest {

    private lateinit var settingsPreferencesDataSourceImpl: SettingsPreferencesDataSourceImpl
    private var preferencesDataStore: DataStore<Preferences> = mock()
    private val protoDataStoreMock: DataStore<UserPreferences> = mock()
    private val hideTagDataStoreMock: DataStore<HideTag> = mock()
    private val rememberUserProtoDataStoreMock: DataStore<RememberUserPreferences> = mock()
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val COMPACT_VIEW_KEY = booleanPreferencesKey("compact_view")
    private val CATEGORIES_VISIBLE_KEY = booleanPreferencesKey("categories_visible")
    private val SELECTED_CATEGORIES_KEY = stringPreferencesKey("selected_categories")

    @BeforeEach
    fun setUp() {
        settingsPreferencesDataSourceImpl = SettingsPreferencesDataSourceImpl(
            dataStore = preferencesDataStore,
            protoDataStore = protoDataStoreMock,
            rememberUserProtoDataStore = rememberUserProtoDataStoreMock,
            hideTagDataStore = hideTagDataStoreMock
            )
    }

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
        verify(preferencesDataStore).edit(any())
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

    @Test
    fun `getCompactView returns expected value`() = runTest {
        val expectedValue = true
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(COMPACT_VIEW_KEY to expectedValue)))
        val actualValue = settingsPreferencesDataSourceImpl.getCompactView()
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `setCompactView updates compact view to false`() = runTest {
        val compactView = false
        settingsPreferencesDataSourceImpl.setCompactView(compactView)
        verify(preferencesDataStore).edit(any())
    }

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
        verify(preferencesDataStore).edit(any())
    }

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

    @Test
    fun `resetRememberUser resets remembered user data correctly`() = runTest {
        settingsPreferencesDataSourceImpl.resetRememberUser()
        verify(rememberUserProtoDataStoreMock).updateData(any())
    }

    @Test
    fun `setMakeArchivePublic updates preference correctly`() = runTest {
        val newValue = true
        settingsPreferencesDataSourceImpl.setMakeArchivePublic(newValue)
        verify(rememberUserProtoDataStoreMock).updateData(any())
    }

    @Test
    fun `getMakeArchivePublic retrieves the correct value`() = runTest {
        val expectedValue = true
        whenever(rememberUserProtoDataStoreMock.data).thenReturn(flowOf(RememberUserPreferences.getDefaultInstance().toBuilder().setMakeArchivePublic(expectedValue).build()))
        val actualValue = settingsPreferencesDataSourceImpl.getMakeArchivePublic()
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `setCreateEbook updates preference correctly`() = runTest {
        val newValue = true
        settingsPreferencesDataSourceImpl.setCreateEbook(newValue)
        verify(rememberUserProtoDataStoreMock).updateData(any())
    }

    @Test
    fun `getCreateEbook retrieves the correct value`() = runTest {
        val expectedValue = false
        whenever(rememberUserProtoDataStoreMock.data).thenReturn(flowOf(RememberUserPreferences.getDefaultInstance().toBuilder().setCreateEbook(expectedValue).build()))
        val actualValue = settingsPreferencesDataSourceImpl.getCreateEbook()
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `setCategoriesVisible updates categories visible preference correctly`() = runTest {
        val isVisible = true
        settingsPreferencesDataSourceImpl.setCategoriesVisible(isVisible)
        verify(preferencesDataStore).edit(any())
    }

    @Test
    fun `getCategoriesVisible retrieves the correct value`() = runTest {
        val expectedValue = false
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(CATEGORIES_VISIBLE_KEY to expectedValue)))
        val actualValue = settingsPreferencesDataSourceImpl.getCategoriesVisible()
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `setSelectedCategories updates selected categories correctly`() = runTest {
        val selectedCategories = listOf("Sports", "Technology")
        settingsPreferencesDataSourceImpl.setSelectedCategories(selectedCategories)
        verify(preferencesDataStore).edit(any())
    }

    @Test
    fun `getSelectedCategories retrieves the correct values`() = runTest {
        val expectedCategories = listOf("Sports", "Technology")
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(SELECTED_CATEGORIES_KEY to expectedCategories.joinToString(","))))
        val actualCategories = settingsPreferencesDataSourceImpl.getSelectedCategories()
        assertEquals(expectedCategories, actualCategories)
    }

    @Test
    fun `getUrl retrieves the correct server URL`() = runTest {
        val expectedUrl = "https://example.com"
        whenever(protoDataStoreMock.data).thenReturn(flowOf(UserPreferences.newBuilder().setUrl(expectedUrl).build()))
        val actualUrl = settingsPreferencesDataSourceImpl.getUrl()
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `getSession retrieves the correct session value`() = runTest {
        val expectedSession = "session123"
        whenever(protoDataStoreMock.data).thenReturn(flowOf(UserPreferences.newBuilder().setSession(expectedSession).build()))
        val actualSession = settingsPreferencesDataSourceImpl.getSession()
        assertEquals(expectedSession, actualSession)
    }

    @Test
    fun `getToken retrieves the correct token`() = runTest {
        val expectedToken = "token123"
        whenever(protoDataStoreMock.data).thenReturn(flowOf(UserPreferences.newBuilder().setToken(expectedToken).build()))
        val actualToken = settingsPreferencesDataSourceImpl.getToken()
        assertEquals(expectedToken, actualToken)
    }

    @Test
    fun `getIsLegacyApi retrieves the correct flag for legacy API usage`() = runTest {
        val expectedIsLegacyApi = true
        whenever(protoDataStoreMock.data).thenReturn(flowOf(UserPreferences.newBuilder().setIsLegacyApi(expectedIsLegacyApi).build()))
        val actualIsLegacyApi = settingsPreferencesDataSourceImpl.getIsLegacyApi()
        assertEquals(expectedIsLegacyApi, actualIsLegacyApi)
    }

    @Test
    fun `setCreateArchive updates preference correctly`() = runTest {
        val newValue = true
        settingsPreferencesDataSourceImpl.setCreateArchive(newValue)
        verify(rememberUserProtoDataStoreMock).updateData(any())
    }

    @Test
    fun `getCreateArchive retrieves the correct value`() = runTest {
        val expectedValue = true
        whenever(rememberUserProtoDataStoreMock.data).thenReturn(flowOf(RememberUserPreferences.newBuilder().setCreateArchive(expectedValue).build()))
        val actualValue = settingsPreferencesDataSourceImpl.getCreateArchive()
        assertEquals(expectedValue, actualValue)
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

    @Test
    fun `setUseDynamicColors updates preference correctly`() = runTest {
        val newValue = true
        settingsPreferencesDataSourceImpl.setUseDynamicColors(newValue)
        verify(preferencesDataStore).edit(any())
    }

    @Test
    fun `getUseDynamicColors retrieves the correct value`() = runTest {
        val expectedValue = true
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(settingsPreferencesDataSourceImpl.USE_DYNAMIC_COLORS to expectedValue)))
        val actualValue = settingsPreferencesDataSourceImpl.getUseDynamicColors()
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `setHideTag updates HideTag correctly`() = runTest {
        val tag = Tag(id = 1, name = "TestTag", selected = false, nBookmarks = 0)
        settingsPreferencesDataSourceImpl.setHideTag(tag)
        verify(hideTagDataStoreMock).updateData(any())
    }

    @Test
    fun `setHideTag with null clears HideTag`() = runTest {
        settingsPreferencesDataSourceImpl.setHideTag(null)
        verify(hideTagDataStoreMock).updateData(any())
    }

    @Test
    fun `getHideTag retrieves the correct Tag when set`() = runTest {
        val expectedTag = HideTag.newBuilder()
            .setId(1)
            .setName("TestTag")
            .build()
        whenever(hideTagDataStoreMock.data).thenReturn(flowOf(expectedTag))
        val actualTag = settingsPreferencesDataSourceImpl.getHideTag()
        assertNotNull(actualTag)
        assertEquals(expectedTag.id, actualTag?.id)
        assertEquals(expectedTag.name, actualTag?.name)
    }

    @Test
    fun `getHideTag returns null when no tag is set`() = runTest {
        whenever(hideTagDataStoreMock.data).thenReturn(flowOf(HideTag.getDefaultInstance()))
        val actualTag = settingsPreferencesDataSourceImpl.getHideTag()
        assertNull(actualTag)
    }

}
