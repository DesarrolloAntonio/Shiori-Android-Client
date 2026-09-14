package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

/**
 * Logout empties the local database and cancels the sync queue. It used to run on the first tap,
 * so offline a bookmark that had never reached the server was deleted from the device, its upload
 * cancelled, and it was gone for good once the network came back. Seen on a device.
 *
 * Now the tap only asks, and says how many changes are still waiting.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelLogoutConfirmationTest {

    private val dispatcher = StandardTestDispatcher()

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

    private val sendLogoutUseCase: SendLogoutUseCase = mock<SendLogoutUseCase>().stub {
        on { invoke(any(), any()) } doReturn flowOf(Result.Success("ok"))
    }

    private val pendingCreate = PendingJob(
        operationType = SyncOperationType.CREATE,
        state = "ENQUEUED",
        bookmarkId = 1_789_383_854,
        bookmarkTitle = "",
    )
    private val syncWorks: SyncWorks = mock<SyncWorks>().stub {
        on { getPendingJobs() } doReturn flowOf(listOf(pendingCreate))
    }

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
        imageLoader = mock<ImageLoader>(),
        syncWorks = syncWorks,
    )

    @Test
    fun `tapping logout asks first and says how many changes are waiting`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.requestLogout()
        testScheduler.advanceUntilIdle()

        assertEquals(1, vm.logoutConfirmation.value)
        verify(sendLogoutUseCase, never()).invoke(any(), any())
    }

    @Test
    fun `cancelling the confirmation logs nobody out`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.requestLogout()
        testScheduler.advanceUntilIdle()
        vm.cancelLogout()
        testScheduler.advanceUntilIdle()

        assertNull(vm.logoutConfirmation.value)
        verify(sendLogoutUseCase, never()).invoke(any(), any())
    }

    /**
     * The use case has already wiped the session when the server refuses the logout. An HTTP error
     * carries no throwable, so the message came out empty, no dialog showed and nothing moved the
     * user off Settings: signed out, still looking signed in. Seen on a device against a 401.
     */
    @Test
    fun `a logout the server refuses still reports something`() = runTest(dispatcher) {
        sendLogoutUseCase.stub {
            on { invoke(any(), any()) } doReturn flowOf(Result.Loading(null), Result.Error(Result.ErrorType.HttpError(statusCode = 401, message = """{"ok":false,"message":"unauthorized"}""")))
        }
        val vm = viewModel()

        vm.confirmLogout()
        testScheduler.advanceUntilIdle()

        assertTrue(!vm.logoutUiState.value.error.isNullOrEmpty(), "no message, so the screen never leaves Settings")
    }

    /** The pair (R7): confirming does log out. */
    @Test
    fun `confirming logs out`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.requestLogout()
        testScheduler.advanceUntilIdle()
        vm.confirmLogout()
        testScheduler.advanceUntilIdle()

        assertNull(vm.logoutConfirmation.value)
        verify(sendLogoutUseCase).invoke(any(), any())
    }
}
