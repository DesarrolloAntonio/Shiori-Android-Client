package com.desarrollodroide.data.local.preferences

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import java.time.ZoneId
import java.time.ZonedDateTime

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

    // --- Dynamic Colors Tests ---

    @Test
    fun `getUseDynamicColors returns expected value when set`() = runTest {
        val expectedValue = true
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf(USE_DYNAMIC_COLORS to expectedValue)))

        val actualValue = settingsPreferencesDataSourceImpl.getUseDynamicColors()

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `getUseDynamicColors returns false by default when not set`() = runTest {
        whenever(preferencesDataStore.data).thenReturn(flowOf(preferencesOf()))

        val actualValue = settingsPreferencesDataSourceImpl.getUseDynamicColors()

        assertFalse(actualValue)
    }

    @Test
    fun `setUseDynamicColors updates preference correctly`() = runTest {
        val newValue = true

        settingsPreferencesDataSourceImpl.setUseDynamicColors(newValue)

        verifyPreferenceEdit(preferencesDataStore, USE_DYNAMIC_COLORS, newValue)
    }

    @Test
    fun `setUseDynamicColors can disable dynamic colors`() = runTest {
        val newValue = false

        settingsPreferencesDataSourceImpl.setUseDynamicColors(newValue)

        verifyPreferenceEdit(preferencesDataStore, USE_DYNAMIC_COLORS, newValue)
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
        settingsPreferencesDataSourceImpl.resetData()
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

    // --- CompactView Tests ---

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
    fun `setCompactView updates compact view preference correctly`() = runTest {
        // Given
        val newCompactViewValue = true
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setCompactView(newCompactViewValue)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(newCompactViewValue, updatedPreferences.compactView)
    }

    @Test
    fun `setCompactView toggles from true to false correctly`() = runTest {
        // Given
        val initialPreferences = SystemPreferences.newBuilder()
            .setCompactView(true)
            .build()
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setCompactView(false)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val updatedPreferences = captor.firstValue.invoke(initialPreferences)
        assertFalse(updatedPreferences.compactView)
    }

    @Test
    fun `compact view state is correctly propagated through flow`() = runTest {
        // Given
        val initialPreferences = SystemPreferences.newBuilder()
            .setCompactView(true)
            .build()
        val updatedPreferences = SystemPreferences.newBuilder()
            .setCompactView(false)
            .build()

        // Create a flow that will emit both values
        val preferencesFlow = flowOf(initialPreferences, updatedPreferences)
        whenever(systemPreferencesDataStoreMock.data).thenReturn(preferencesFlow)

        // Then
        settingsPreferencesDataSourceImpl.compactViewFlow.test {
            assertEquals(true, awaitItem()) // First emission
            assertEquals(false, awaitItem()) // Second emission
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

    // --- AutoAddBookmark Tests ---

    @Test
    fun `setAutoAddBookmark updates preference correctly`() = runTest {
        // Given
        val newValue = true
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setAutoAddBookmark(newValue)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(newValue, updatedPreferences.autoAddBookmark)
    }

    @Test
    fun `setAutoAddBookmark can disable auto-add bookmark`() = runTest {
        // Given
        val initialPreferences = SystemPreferences.newBuilder()
            .setAutoAddBookmark(true)
            .build()
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setAutoAddBookmark(false)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val updatedPreferences = captor.firstValue.invoke(initialPreferences)
        assertFalse(updatedPreferences.autoAddBookmark)
    }

    @Test
    fun `autoAddBookmarkFlow emits correct values`() = runTest {
        // Given
        val mockSystemPreferences = SystemPreferences.newBuilder()
            .setAutoAddBookmark(true)
            .build()
        val mockSystemPreferencesFlow: Flow<SystemPreferences> = flowOf(mockSystemPreferences)
        whenever(systemPreferencesDataStoreMock.data).thenReturn(mockSystemPreferencesFlow)

        // Then
        settingsPreferencesDataSourceImpl.autoAddBookmarkFlow.test {
            val emittedItem = awaitItem()
            assertTrue(emittedItem)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `autoAddBookmarkFlow emits updates when preference changes`() = runTest {
        // Given
        val initialPreferences = SystemPreferences.newBuilder()
            .setAutoAddBookmark(false)
            .build()
        val updatedPreferences = SystemPreferences.newBuilder()
            .setAutoAddBookmark(true)
            .build()
        val preferencesFlow = flowOf(initialPreferences, updatedPreferences)
        whenever(systemPreferencesDataStoreMock.data).thenReturn(preferencesFlow)

        // Then
        settingsPreferencesDataSourceImpl.autoAddBookmarkFlow.test {
            assertFalse(awaitItem()) // Initial value
            assertTrue(awaitItem())  // Updated value
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- CreateArchive Tests ---

    @Test
    fun `setCreateArchive updates preference correctly`() = runTest {
        // Given
        val newValue = true
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setCreateArchive(newValue)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(newValue, updatedPreferences.createArchive)
    }

    @Test
    fun `setCreateArchive can disable archive creation`() = runTest {
        // Given
        val initialPreferences = SystemPreferences.newBuilder()
            .setCreateArchive(true)
            .build()
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setCreateArchive(false)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val updatedPreferences = captor.firstValue.invoke(initialPreferences)
        assertFalse(updatedPreferences.createArchive)
    }

    @Test
    fun `createArchiveFlow emits initial value correctly`() = runTest {
        // Given
        val mockSystemPreferences = SystemPreferences.newBuilder()
            .setCreateArchive(true)
            .build()
        val mockSystemPreferencesFlow: Flow<SystemPreferences> = flowOf(mockSystemPreferences)
        whenever(systemPreferencesDataStoreMock.data).thenReturn(mockSystemPreferencesFlow)

        // Then
        settingsPreferencesDataSourceImpl.createArchiveFlow.test {
            val emittedItem = awaitItem()
            assertTrue(emittedItem)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createArchiveFlow reflects preference changes`() = runTest {
        // Given
        val initialPreferences = SystemPreferences.newBuilder()
            .setCreateArchive(false)
            .build()
        val updatedPreferences = SystemPreferences.newBuilder()
            .setCreateArchive(true)
            .build()
        val preferencesFlow = flowOf(initialPreferences, updatedPreferences)
        whenever(systemPreferencesDataStoreMock.data).thenReturn(preferencesFlow)

        // Then
        settingsPreferencesDataSourceImpl.createArchiveFlow.test {
            assertFalse(awaitItem()) // Initial value
            assertTrue(awaitItem())  // Updated value
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- User Preferences Getters Tests ---

    // Tests for getUrl()
    @Test
    fun `getUrl returns correct server url from user preferences`() = runTest {
        // Given
        val expectedUrl = "https://example.com"
        val userPreferences = UserPreferences.newBuilder()
            .setUrl(expectedUrl)
            .build()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(userPreferences))

        // When
        val actualUrl = settingsPreferencesDataSourceImpl.getUrl()

        // Then
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `getUrl returns empty string when no url is set`() = runTest {
        // Given
        val userPreferences = UserPreferences.getDefaultInstance()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(userPreferences))

        // When
        val actualUrl = settingsPreferencesDataSourceImpl.getUrl()

        // Then
        assertEquals("", actualUrl)
    }

    // Tests for getSession()
    @Test
    fun `getSession returns correct session from user preferences`() = runTest {
        // Given
        val expectedSession = "session123"
        val userPreferences = UserPreferences.newBuilder()
            .setSession(expectedSession)
            .build()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(userPreferences))

        // When
        val actualSession = settingsPreferencesDataSourceImpl.getSession()

        // Then
        assertEquals(expectedSession, actualSession)
    }

    @Test
    fun `getSession returns empty string when no session is set`() = runTest {
        // Given
        val userPreferences = UserPreferences.getDefaultInstance()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(userPreferences))

        // When
        val actualSession = settingsPreferencesDataSourceImpl.getSession()

        // Then
        assertEquals("", actualSession)
    }

    // Tests for getToken()
    @Test
    fun `getToken returns correct token from user preferences`() = runTest {
        // Given
        val expectedToken = "token123"
        val userPreferences = UserPreferences.newBuilder()
            .setToken(expectedToken)
            .build()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(userPreferences))

        // When
        val actualToken = settingsPreferencesDataSourceImpl.getToken()

        // Then
        assertEquals(expectedToken, actualToken)
    }

    @Test
    fun `getToken returns empty string when no token is set`() = runTest {
        // Given
        val userPreferences = UserPreferences.getDefaultInstance()
        whenever(protoDataStoreMock.data).thenReturn(flowOf(userPreferences))

        // When
        val actualToken = settingsPreferencesDataSourceImpl.getToken()

        // Then
        assertEquals("", actualToken)
    }

    // --- Hide Tag Tests ---

    // Tests for setHideTag()
    @Test
    fun `setHideTag updates tag correctly`() = runTest {
        // Given
        val tag = Tag(id = 1, name = "TestTag", selected = false, nBookmarks = 0)
        val captor = argumentCaptor<suspend (HideTag) -> HideTag>()

        // When
        settingsPreferencesDataSourceImpl.setHideTag(tag)

        // Then
        verify(hideTagDataStoreMock).updateData(captor.capture())
        val testHideTag = HideTag.getDefaultInstance()
        val updatedHideTag = captor.firstValue.invoke(testHideTag)
        assertEquals(tag.id, updatedHideTag.id)
        assertEquals(tag.name, updatedHideTag.name)
    }

    @Test
    fun `setHideTag handles null tag by returning default instance`() = runTest {
        // Given
        val captor = argumentCaptor<suspend (HideTag) -> HideTag>()

        // When
        settingsPreferencesDataSourceImpl.setHideTag(null)

        // Then
        verify(hideTagDataStoreMock).updateData(captor.capture())
        val testHideTag = HideTag.getDefaultInstance()
        val updatedHideTag = captor.firstValue.invoke(testHideTag)
        assertEquals(HideTag.getDefaultInstance(), updatedHideTag)
    }

    // Tests for hideTagFlow
    @Test
    fun `hideTagFlow emits null when no tag is set`() = runTest {
        // Given
        val defaultHideTag = HideTag.getDefaultInstance()
        whenever(hideTagDataStoreMock.data).thenReturn(flowOf(defaultHideTag))

        // Then
        settingsPreferencesDataSourceImpl.hideTagFlow.test {
            val emittedItem = awaitItem()
            assertNull(emittedItem)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hideTagFlow emits correct tag when set`() = runTest {
        // Given
        val expectedTag = HideTag.newBuilder()
            .setId(1)
            .setName("TestTag")
            .build()
        whenever(hideTagDataStoreMock.data).thenReturn(flowOf(expectedTag))

        // Then
        settingsPreferencesDataSourceImpl.hideTagFlow.test {
            val emittedItem = awaitItem()
            assertNotNull(emittedItem)
            assertEquals(expectedTag.id, emittedItem?.id)
            assertEquals(expectedTag.name, emittedItem?.name)
            assertEquals(false, emittedItem?.selected)
            assertEquals(0, emittedItem?.nBookmarks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hideTagFlow reflects changes in hide tag`() = runTest {
        // Given
        val initialTag = HideTag.getDefaultInstance()
        val updatedTag = HideTag.newBuilder()
            .setId(1)
            .setName("UpdatedTag")
            .build()
        val tagsFlow = flowOf(initialTag, updatedTag)
        whenever(hideTagDataStoreMock.data).thenReturn(tagsFlow)

        // Then
        settingsPreferencesDataSourceImpl.hideTagFlow.test {
            assertNull(awaitItem()) // Initial null value
            val updatedItem = awaitItem()
            assertNotNull(updatedItem)
            assertEquals(updatedTag.id, updatedItem?.id)
            assertEquals(updatedTag.name, updatedItem?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Sync Timestamp Tests ---

    // Tests for getLastSyncTimestamp()
    @Test
    fun `getLastSyncTimestamp returns correct timestamp`() = runTest {
        // Given
        val expectedTimestamp = 1234567890L
        val systemPreferences = SystemPreferences.newBuilder()
            .setLastSyncTimestamp(expectedTimestamp)
            .build()
        whenever(systemPreferencesDataStoreMock.data).thenReturn(flowOf(systemPreferences))

        // When
        val actualTimestamp = settingsPreferencesDataSourceImpl.getLastSyncTimestamp()

        // Then
        assertEquals(expectedTimestamp, actualTimestamp)
    }

    // Tests for setLastSyncTimestamp()
    @Test
    fun `setLastSyncTimestamp updates timestamp correctly`() = runTest {
        // Given
        val newTimestamp = 1234567890L
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setLastSyncTimestamp(newTimestamp)

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)
        assertEquals(newTimestamp, updatedPreferences.lastSyncTimestamp)
    }

    // Tests for setCurrentTimeStamp()
    @Test
    fun `setCurrentTimeStamp updates timestamp with current time`() = runTest {
        // Given
        val captor = argumentCaptor<suspend (SystemPreferences) -> SystemPreferences>()

        // When
        settingsPreferencesDataSourceImpl.setCurrentTimeStamp()

        // Then
        verify(systemPreferencesDataStoreMock).updateData(captor.capture())
        val testPreferences = SystemPreferences.getDefaultInstance()
        val updatedPreferences = captor.firstValue.invoke(testPreferences)

        // Verify timestamp is recent (within last minute)
        val currentTime = ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond()
        val timestampDiff = currentTime - updatedPreferences.lastSyncTimestamp
        assertTrue(timestampDiff < 60) // Difference should be less than 60 seconds
    }

    // --- Selected Categories Flow Tests ---
    // TODO

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