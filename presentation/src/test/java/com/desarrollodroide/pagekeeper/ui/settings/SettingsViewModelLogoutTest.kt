package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.after
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

/**
 * Logout has to take the image caches with it.
 *
 * The database is emptied on the way out, so leaving Coil's caches behind means the next account
 * to sign in on the device inherits the previous one's thumbnails. They are keyed by url, with
 * nothing tying an entry to an account.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelLogoutTest {

    private val dispatcher = StandardTestDispatcher()

    private val diskCache: DiskCache = mock()
    private val memoryCache: MemoryCache = mock()
    private val imageLoader: ImageLoader = mock<ImageLoader>().stub {
        on { this.diskCache } doReturn diskCache
        on { this.memoryCache } doReturn memoryCache
    }

    // Every Flow the view model reads eagerly in a property initialiser has to be stubbed: an
    // unstubbed mock hands back null and stateIn then dereferences it.
    private val preferences: SettingsPreferenceDataSource = mock<SettingsPreferenceDataSource>().stub {
        onBlocking { getUrl() } doReturn "http://test.com"
        onBlocking { getSession() } doReturn "session"
        onBlocking { getToken() } doReturn "token"
        onBlocking { getThemeMode() } doReturn ThemeMode.AUTO
        onBlocking { getUseDynamicColors() } doReturn false
        onBlocking { getServerVersion() } doReturn "1.8.0"
        on { compactViewFlow } doReturn flowOf(false)
        on { useTwoPaneLayoutFlow } doReturn flowOf(false)
        on { makeArchivePublicFlow } doReturn flowOf(false)
        on { createEbookFlow } doReturn flowOf(false)
        on { autoAddBookmarkFlow } doReturn flowOf(false)
        on { createArchiveFlow } doReturn flowOf(false)
        on { hideTagFlow } doReturn flowOf(null)
    }

    private val themeManager: ThemeManager = object : ThemeManager {
        override var themeMode = mutableStateOf(ThemeMode.AUTO)
        override var useDynamicColors = mutableStateOf(false)
    }

    private val sendLogoutUseCase: SendLogoutUseCase = mock()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(
        sendLogoutUseCase = sendLogoutUseCase,
        bookmarksRepository = mock<BookmarksRepository>(),
        settingsPreferenceDataSource = preferences,
        themeManager = themeManager,
        getTagsUseCase = mock<GetTagsUseCase>(),
        imageLoader = imageLoader,
    )

    @Test
    fun `logging out clears the image caches`() = runTest(dispatcher) {
        sendLogoutUseCase.stub {
            on { invoke(any(), any()) } doReturn flowOf(Result.Success("ok"))
        }

        viewModel().logout()
        testScheduler.advanceUntilIdle()

        verifyCachesCleared()
    }

    /**
     * A logout the server never answered still signs you out locally — the use case wipes the
     * database on its error branch too — so the caches have to go on that path as well.
     */
    @Test
    fun `a failed logout still clears the image caches`() = runTest(dispatcher) {
        sendLogoutUseCase.stub {
            on { invoke(any(), any()) } doReturn flowOf(
                Result.Error(Result.ErrorType.IOError(Exception("no network")))
            )
        }

        viewModel().logout()
        testScheduler.advanceUntilIdle()

        verifyCachesCleared()
    }

    @Test
    fun `merely opening settings does not clear the caches`() = runTest(dispatcher) {
        viewModel()
        testScheduler.advanceUntilIdle()

        // after(), not never() on its own: the clearing runs off the test scheduler, so an
        // immediate never() would pass simply by checking too early.
        verify(diskCache, after(TIMEOUT_MS).never()).clear()
        verify(memoryCache, never()).clear()
    }

    /**
     * ImageLoader.clearCache switches to Dispatchers.IO, which the test scheduler does not drive,
     * so advanceUntilIdle returns before the caches have actually been touched. Verifying straight
     * afterwards made this pass or fail depending on which thread got there first.
     */
    private fun verifyCachesCleared() {
        verify(diskCache, timeout(TIMEOUT_MS)).clear()
        verify(memoryCache, timeout(TIMEOUT_MS)).clear()
    }

    private companion object {
        const val TIMEOUT_MS = 2_000L
    }
}
