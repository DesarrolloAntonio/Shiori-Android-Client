package com.desarrollodroide.pagekeeper.ui.feed

import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncStatus
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.data.repository.TagsRepository
import com.desarrollodroide.domain.usecase.DeleteBookmarkUseCase
import com.desarrollodroide.domain.usecase.DeleteLocalBookmarkUseCase
import com.desarrollodroide.domain.usecase.DownloadFileUseCase
import com.desarrollodroide.domain.usecase.GetAllRemoteBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetLocalPagingBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.UpdateBookmarkCacheUseCase
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

/**
 * How often the app walks the whole server.
 *
 * Every sync entry point — the initial load, pull to refresh, saving an edit, adding tags to a
 * selection — runs the same full paginated walk of every page. Overlapping them means duplicate
 * requests and two writers on the same tables, and the initial load used to do exactly that.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedSyncTest {

    private val dispatcher = StandardTestDispatcher()

    private var syncCount = 0
    private var databaseEmpty = true

    private val bookmarksDao: BookmarksDao = mock<BookmarksDao>().stub {
        onBlocking { isEmpty() } doAnswer { databaseEmpty }
    }

    private val preferences: SettingsPreferenceDataSource = mock<SettingsPreferenceDataSource>().stub {
        onBlocking { getUrl() } doReturn "http://test.com"
        onBlocking { getToken() } doReturn "token"
        onBlocking { getSession() } doReturn "session"
        on { useTwoPaneLayoutFlow } doReturn flowOf(false)
        on { compactViewFlow } doReturn flowOf(false)
        on { hideTagFlow } doReturn flowOf(null)
        on { selectedCategoriesFlow } doReturn flowOf(emptyList())
    }

    private val getTagsUseCase: GetTagsUseCase = mock<GetTagsUseCase>().stub {
        on { getLocalTags() } doReturn flowOf(emptyList())
        on { invoke(any(), any()) } doReturn emptyFlow()
    }

    private val getLocalPagingBookmarksUseCase: GetLocalPagingBookmarksUseCase =
        mock<GetLocalPagingBookmarksUseCase>().stub {
            // tagToHide is nullable and null here, and any() does not match null.
            on { invoke(any(), any(), any(), any(), any(), anyOrNull()) } doReturn flowOf(PagingData.empty())
        }

    /**
     * Counts calls, and takes long enough that a second call made while it runs would overlap
     * rather than tidily follow.
     */
    private val getAllRemoteBookmarksUseCase: GetAllRemoteBookmarksUseCase =
        mock<GetAllRemoteBookmarksUseCase>().stub {
            onBlocking { invoke(any(), any()) } doReturn flow {
                syncCount++
                delay(1_000)
                emit(Result.success(SyncStatus.Completed(0) as SyncStatus))
            }
        }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        syncCount = 0
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = FeedViewModel(
        bookmarkDatabase = bookmarksDao,
        settingsPreferenceDataSource = preferences,
        getTagsUseCase = getTagsUseCase,
        getLocalPagingBookmarksUseCase = getLocalPagingBookmarksUseCase,
        deleteBookmarkUseCase = mock<DeleteBookmarkUseCase>(),
        updateBookmarkCacheUseCase = mock<UpdateBookmarkCacheUseCase>(),
        downloadFileUseCase = mock<DownloadFileUseCase>(),
        getAllRemoteBookmarksUseCase = getAllRemoteBookmarksUseCase,
        deleteLocalBookmarkUseCase = mock<DeleteLocalBookmarkUseCase>(),
        syncManager = mock<SyncWorks>(),
        bookmarksRepository = mock<BookmarksRepository>(),
        tagsRepository = mock<TagsRepository>(),
    )

    /**
     * The regression. An empty database is what first login looks like, and that branch used to
     * start a sync of its own before the shared one below it ran, so the very first start — with
     * the most to download — issued every request twice.
     */
    @Test
    fun `first login syncs once, not twice`() = runTest(dispatcher) {
        databaseEmpty = true

        viewModel().initializeIfNeeded()
        testScheduler.advanceUntilIdle()

        assertEquals(1, syncCount, "first login must walk the server once")
    }

    @Test
    fun `a later start also syncs once`() = runTest(dispatcher) {
        databaseEmpty = false

        viewModel().initializeIfNeeded()
        testScheduler.advanceUntilIdle()

        assertEquals(1, syncCount, "a start with a populated cache must still sync once")
    }

    /**
     * Pull to refresh while a sync is running should not start a second walk. Different cause from
     * the first-login bug, same cost.
     */
    @Test
    fun `refreshing while a sync runs does not start another`() = runTest(dispatcher) {
        databaseEmpty = false
        val viewModel = viewModel()

        viewModel.refreshFeed()
        testScheduler.advanceTimeBy(100)
        viewModel.refreshFeed()
        viewModel.refreshFeed()
        testScheduler.advanceUntilIdle()

        assertEquals(1, syncCount, "overlapping refreshes must collapse into the running sync")
    }

    /** Once it has finished, refreshing has to work again. */
    @Test
    fun `refreshing after a sync finishes starts a new one`() = runTest(dispatcher) {
        databaseEmpty = false
        val viewModel = viewModel()

        viewModel.refreshFeed()
        testScheduler.advanceUntilIdle()
        viewModel.refreshFeed()
        testScheduler.advanceUntilIdle()

        assertEquals(2, syncCount, "the guard must not wedge refresh shut once the sync is done")
    }
}
