package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

/**
 * "Hide tag" puts a spinner up while the tags load, and that dialog cannot be dismissed.
 *
 * The error branch used to only log, so when the server could not be reached the state stayed
 * loading for good and the only way out was to kill the app. Seen on a device with the server
 * unreachable. Hiding a tag is a local setting, so the tags already stored on the device are
 * enough to offer when the refresh fails.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelHideTagTest {

    private val dispatcher = StandardTestDispatcher()

    private val preferences: SettingsPreferenceDataSource = mock<SettingsPreferenceDataSource>().stub {
        onBlocking { getUrl() } doReturn "http://test.com"
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

    private val getTagsUseCase: GetTagsUseCase = mock()

    private val storedTags = listOf(Tag(1, "kotlin"), Tag(2, "android"))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(
        sendLogoutUseCase = mock<SendLogoutUseCase>(),
        bookmarksRepository = mock<BookmarksRepository>(),
        settingsPreferenceDataSource = preferences,
        themeManager = themeManager,
        getTagsUseCase = getTagsUseCase,
        imageLoader = mock<ImageLoader>(),
        syncWorks = mock(),
        networkLogger = mock(),
    )

    private fun tagsAnswer(flow: Flow<Result<List<Tag>?>>) = getTagsUseCase.stub {
        on { invoke(any(), any()) } doReturn flow
    }

    @Test
    fun `an unreachable server with tags on the device offers the stored tags`() = runTest(dispatcher) {
        tagsAnswer(flowOf(Result.Loading(null), Result.Loading(storedTags), Result.Error(Result.ErrorType.IOError(Exception("refused")))))
        val vm = viewModel()

        vm.getTags()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.tagsState.value.isLoading, "still loading after the request failed")
        assertEquals(storedTags, vm.tagsState.value.data)
    }

    @Test
    fun `an unreachable server with no tags on the device ends the spinner with an error`() = runTest(dispatcher) {
        tagsAnswer(flowOf(Result.Loading(null), Result.Loading(emptyList()), Result.Error(Result.ErrorType.IOError(Exception("refused")))))
        val vm = viewModel()

        vm.getTags()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.tagsState.value.isLoading, "still loading after the request failed")
        assertNotNull(vm.tagsState.value.error)
    }

    /** The pair (R7): when the server answers, its tags are what the sheet gets. */
    @Test
    fun `a successful refresh offers the server's tags`() = runTest(dispatcher) {
        val fromServer = storedTags + Tag(3, "compose")
        tagsAnswer(flowOf(Result.Loading(null), Result.Loading(storedTags), Result.Success(fromServer)))
        val vm = viewModel()

        vm.getTags()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.tagsState.value.isLoading)
        assertEquals(fromServer, vm.tagsState.value.data)
    }
}
