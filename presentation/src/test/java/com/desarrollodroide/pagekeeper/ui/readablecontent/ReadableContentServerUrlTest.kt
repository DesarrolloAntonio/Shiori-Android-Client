package com.desarrollodroide.pagekeeper.ui.readablecontent

import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.local.room.dao.BookmarkHtmlDao
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.domain.usecase.GetBookmarkReadableContentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

/**
 * Which server the reader asks for an article.
 *
 * The screen used to call two functions in a row: one launched a coroutine that read the server
 * url out of DataStore into a field, the other launched a coroutine that read that field back.
 * `launch` dispatches rather than running inline, so the reader always ran while the writer was
 * still suspended on the DataStore read, and the url was always still empty. Not a flaky race —
 * it lost every time.
 *
 * Retrofit is built with a placeholder base url, because every endpoint passes an absolute `@Url`.
 * So an empty server url did not fail: it resolved against the placeholder, and every article
 * request in the app went out to google.com, whose 404 page the app then tried to read as
 * Shiori's answer. Nothing about that was visible on screen — it said "No local content
 * available", which is also what it says when the cache is simply empty.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReadableContentServerUrlTest {

    private val dispatcher = StandardTestDispatcher()

    private var requestedServerUrl: String? = null
    private var requestedToken: String? = null

    /**
     * Preferences that really suspend, which is what makes this test able to fail.
     *
     * A mocked suspend function answers without ever suspending, so a coroutine reading the
     * preferences runs to completion before anything else is dispatched, and the broken ordering
     * cannot happen. DataStore, the real implementation, suspends. Delegation covers the other
     * forty-odd members of the interface; only the two reads on this path need to be honest.
     */
    private class SuspendingPreferences(
        private val delegate: SettingsPreferenceDataSource,
    ) : SettingsPreferenceDataSource by delegate {
        override suspend fun getUrl(): String {
            yield()
            return delegate.getUrl()
        }

        override suspend fun getToken(): String {
            yield()
            return delegate.getToken()
        }
    }

    private val preferences: SettingsPreferenceDataSource = SuspendingPreferences(
        mock<SettingsPreferenceDataSource>().stub {
            onBlocking { getUrl() } doReturn "https://shiori.example.com"
            onBlocking { getToken() } doReturn "the-token"
            on { getThemeMode() } doReturn ThemeMode.DARK
        }
    )

    private val useCase: GetBookmarkReadableContentUseCase =
        mock<GetBookmarkReadableContentUseCase>().stub {
            on { invoke(any(), any(), any()) } doAnswer { invocation ->
                requestedServerUrl = invocation.arguments[0] as String
                requestedToken = invocation.arguments[1] as String
                emptyFlow()
            }
        }

    private fun viewModel() = ReadableContentViewModel(
        settingsPreferenceDataSource = preferences,
        getBookmarkReadableContentUseCase = useCase,
        bookmarksDao = mock<BookmarksDao>(),
        bookmarkHtmlDao = mock<BookmarkHtmlDao>(),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the article is requested from the configured server, not the placeholder`() = runTest(dispatcher) {
        viewModel().load(bookmarkId = 80, bookmarkUrl = "https://kotlinlang.org/docs/coroutines-guide.html")
        advanceUntilIdle()

        assertEquals(
            "https://shiori.example.com",
            requestedServerUrl,
            "an empty url here is a request sent to Retrofit's placeholder host instead of Shiori",
        )
        assertEquals("the-token", requestedToken, "the request must carry the stored token")
    }

    @Test
    fun `the theme is settled before the article arrives`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.load(bookmarkId = 80, bookmarkUrl = "https://kotlinlang.org")
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
    }
}
