package com.desarrollodroide.data.local.preferences

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
import kotlinx.coroutines.flow.flow
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class SettingsPreferencesDataSourceImplTest {

    private lateinit var settingsPreferencesDataSourceImpl: SettingsPreferencesDataSourceImpl
    private var preferencesDataStore: DataStore<Preferences> = mock()
    private val protoDataStoreMock: DataStore<UserPreferences> = mock()
    private val rememberUserProtoDataStoreMock: DataStore<RememberUserPreferences> = mock()
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    @BeforeEach
    fun setUp() {
        settingsPreferencesDataSourceImpl = SettingsPreferencesDataSourceImpl(
            dataStore = preferencesDataStore,
            protoDataStore = protoDataStoreMock,
            rememberUserProtoDataStore = rememberUserProtoDataStoreMock
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

}
