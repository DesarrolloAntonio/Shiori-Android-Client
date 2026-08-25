package com.desarrollodroide.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.map
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.*
import retrofit2.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.check
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.SyncStatus
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.network.model.SingleBookmarkResponseDTO
import com.desarrollodroide.data.local.room.dao.BookmarkHtmlDao
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarksDTO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.mockito.kotlin.anyOrNull
import java.io.IOException

@ExperimentalCoroutinesApi
class BookmarksRepositoryTest {

    @Mock
    private lateinit var apiService: RetrofitNetwork

    @Mock
    private lateinit var bookmarksDao: BookmarksDao

    @Mock
    private lateinit var tagDao: TagDao

    @Mock
    private lateinit var bookmarkHtmlDao: BookmarkHtmlDao

    @Mock
    private lateinit var errorHandler: ErrorHandler

    private lateinit var bookmarksRepository: BookmarksRepositoryImpl

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        bookmarksRepository = BookmarksRepositoryImpl(apiService, bookmarksDao, tagDao, bookmarkHtmlDao, errorHandler)
    }

    @Test
    fun `getBookmarks should emit Loading and Success states when API call is successful`() = runTest {
        // Arrange
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        val bookmarksDTO = BookmarksDTO(
            maxPage = 1,
            page = 1,
            bookmarks = listOf(
                BookmarkDTO(1, "http://bookmark1.com", "Bookmark 1", "Excerpt 1", "Author 1", 1, "2023-01-01","2023-01-02",  "http://image1.com", true, true, true, listOf(), true, true),
                BookmarkDTO(2, "http://bookmark2.com", "Bookmark 2", "Excerpt 2", "Author 2", 1, "2023-01-02", "2023-01-02","http://image2.com", true, true, true, listOf(), true, true)
            )
        )
        val bookmarkEntities = listOf(
            BookmarkEntity(1, "http://bookmark1.com", "Bookmark 1", "Excerpt 1", "Author 1", 1, "2023-01-01", "2023-01-02","http://image1.com", true, true, true, listOf(), true, true),
            BookmarkEntity(2, "http://bookmark2.com", "Bookmark 2", "Excerpt 2", "Author 2", 1, "2023-01-02", "2023-01-02","http://image2.com", true, true, true, listOf(), true, true)
        )
        val expectedBookmarks = bookmarkEntities.map { it.toDomainModel() }

        `when`(apiService.getBookmarks(eq(xSessionId), anyString())).thenReturn(Response.success(bookmarksDTO))
        `when`(bookmarksDao.getAll()).thenReturn(flowOf(bookmarkEntities))

        // Act
        val results = bookmarksRepository.getBookmarks(xSessionId, serverUrl).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null)
        assertTrue(results[1] is Result.Loading && results[1].data != null)
        assertTrue(results[2] is Result.Success && results[2].data == expectedBookmarks)

        // One transactional call, not a delete followed by an insert: a failure between the two
        // used to leave the cache empty.
        verify(bookmarksDao).insertAllWithTags(bookmarkEntities)
        verify(bookmarksDao, never()).deleteAll()
        verify(apiService).getBookmarks(eq(xSessionId), check { it.endsWith("/api/bookmarks") })
    }

    @Test
    fun `getBookmarks should emit Loading and Error states when API call fails`() = runTest {
        // Arrange
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        val errorMessage = "Error fetching bookmarks"
        val errorResponseBody = errorMessage.toResponseBody("text/plain".toMediaTypeOrNull())

        `when`(apiService.getBookmarks(eq(xSessionId), anyString())).thenReturn(Response.error(400, errorResponseBody))
        `when`(errorHandler.getApiError(eq(400), anyOrNull(), eq(errorMessage))).thenReturn(Result.ErrorType.HttpError(statusCode = 400, message = errorMessage))
        `when`(bookmarksDao.getAll()).thenReturn(flowOf(emptyList()))  // Ensure a valid empty flow is returned

        // Act
        val results = bookmarksRepository.getBookmarks(xSessionId, serverUrl).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null)
        assertTrue(results[1] is Result.Loading && results[1].data == emptyList<Bookmark>())
        assertTrue(results[2] is Result.Error && (results[2] as Result.Error).error is Result.ErrorType.HttpError)
        assertEquals((results[2] as Result.Error).error?.message, errorMessage)

        verify(apiService).getBookmarks(eq(xSessionId), check { it.endsWith("/api/bookmarks") })
    }

    @Test
    fun `getBookmarks should emit Loading and Error states when network error occurs`() = runTest {
        // Arrange
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        val networkErrorMessage = "Network error"
        val ioException = IOException(networkErrorMessage)

        `when`(apiService.getBookmarks(eq(xSessionId), anyString())).thenAnswer { throw ioException }
        `when`(errorHandler.getError(ioException)).thenReturn(Result.ErrorType.IOError(ioException))
        `when`(bookmarksDao.getAll()).thenReturn(flowOf(emptyList()))  // Ensure a valid empty flow is returned

        // Act
        val results = bookmarksRepository.getBookmarks(xSessionId, serverUrl).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null)
        assertTrue(results[1] is Result.Loading && results[1].data == emptyList<Bookmark>())
        assertTrue(results[2] is Result.Error && (results[2] as Result.Error).error is Result.ErrorType.IOError)
        assertEquals(networkErrorMessage, (results[2] as Result.Error).error?.throwable?.message)

        verify(apiService).getBookmarks(eq(xSessionId), check { it.endsWith("/api/bookmarks") })
    }

    @Test
    fun `getBookmarks should emit Loading and Error states when API call fails with HTTP error`() = runTest {
        // Arrange
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        val errorMessage = "HTTP error"
        val errorResponseBody = errorMessage.toResponseBody("text/plain".toMediaTypeOrNull())

        `when`(apiService.getBookmarks(eq(xSessionId), anyString())).thenReturn(Response.error(400, errorResponseBody))
        `when`(errorHandler.getApiError(eq(400), anyOrNull(), eq(errorMessage))).thenReturn(Result.ErrorType.HttpError(statusCode = 400, message = errorMessage))
        `when`(bookmarksDao.getAll()).thenReturn(flowOf(emptyList()))  // Ensure a valid empty flow is returned

        // Act
        val results = bookmarksRepository.getBookmarks(xSessionId, serverUrl).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null)
        assertTrue(results[1] is Result.Loading && results[1].data == emptyList<Bookmark>())
        assertTrue(results[2] is Result.Error && (results[2] as Result.Error).error is Result.ErrorType.HttpError)
        assertEquals((results[2] as Result.Error).error?.message, errorMessage)

        verify(apiService).getBookmarks(eq(xSessionId), check { it.endsWith("/api/bookmarks") })
    }

    @Test
    fun `a full sync writes each page as it arrives instead of buffering the library`() = runTest {
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        // Arrange: two pages.
        val page1 = BookmarksDTO(
            page = 1, maxPage = 2, bookmarks = listOf(
                BookmarkDTO(1, "http://a.com", "A", "", "", 1, "2023-01-01", "", "", true, true, true, listOf(), true, true)
            )
        )
        val page2 = BookmarksDTO(
            page = 2, maxPage = 2, bookmarks = listOf(
                BookmarkDTO(2, "http://b.com", "B", "", "", 1, "2023-01-02", "", "", true, true, true, listOf(), true, true)
            )
        )
        `when`(apiService.getPagingBookmarks(eq(xSessionId), anyString()))
            .thenReturn(Response.success(page1), Response.success(page2))

        // Act
        bookmarksRepository.syncAllBookmarks(xSessionId, serverUrl).toList()

        // Assert: one write per page, and the whole-table delete is never used.
        verify(bookmarksDao, times(2)).insertPageWithTags(anyList())
        verify(bookmarksDao, never()).insertAllWithTags(anyList())
        verify(bookmarksDao, never()).deleteAll()
    }

    @Test
    fun `a full sync prunes only what the server stopped returning, and only at the end`() = runTest {
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        val page = BookmarksDTO(
            page = 1, maxPage = 1, bookmarks = listOf(
                BookmarkDTO(7, "http://a.com", "A", "", "", 1, "2023-01-01", "", "", true, true, true, listOf(), true, true)
            )
        )
        `when`(apiService.getPagingBookmarks(eq(xSessionId), anyString())).thenReturn(Response.success(page))

        bookmarksRepository.syncAllBookmarks(xSessionId, serverUrl).toList()

        verify(bookmarksDao).deleteBookmarksNotIn(check { assertEquals(listOf(7), it) })
    }

    @Test
    fun `a sync that fails part way leaves the cache alone rather than emptying it`() = runTest {
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        // Page 1 succeeds, page 2 blows up. The old code deleted everything up front and buffered
        // the rest, so a failure here left the user with nothing to read offline.
        val page1 = BookmarksDTO(
            page = 1, maxPage = 2, bookmarks = listOf(
                BookmarkDTO(1, "http://a.com", "A", "", "", 1, "2023-01-01", "", "", true, true, true, listOf(), true, true)
            )
        )
        `when`(apiService.getPagingBookmarks(eq(xSessionId), anyString()))
            .thenReturn(Response.success(page1))
            .thenThrow(RuntimeException("network died"))

        val statuses = bookmarksRepository.syncAllBookmarks(xSessionId, serverUrl).toList()

        assertTrue(statuses.last() is SyncStatus.Error)
        verify(bookmarksDao).insertPageWithTags(anyList())
        verify(bookmarksDao, never()).deleteBookmarksNotIn(anyList())
        verify(bookmarksDao, never()).deleteAll()
    }

    /**
     * Editing a bookmark can change its tags, so the write has to replace the cross references.
     *
     * The plain update only writes the bookmark row. Tag filtering reads
     * bookmark_tag_cross_ref, so it went on using the tags the bookmark used to have, and the
     * caller papered over it by running a full sync afterwards — a walk of every page of the
     * server to repair a write two lines away. That sync is gone, so this has to be right.
     */
    @Test
    fun `editing a bookmark replaces its tag cross references`() = runTest {
        val edited = BookmarkDTO(
            1, "http://a.com", "A", "", "", 1, "2023-01-01", "2023-01-02", "",
            true, true, true, listOf(), true, true
        )
        `when`(apiService.editBookmark(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(SingleBookmarkResponseDTO(ok = true, message = edited)))

        bookmarksRepository.editBookmark(
            xSession = "session",
            serverUrl = "http://test.com",
            bookmark = edited.toDomainModel(),
        )

        verify(bookmarksDao).updateBookmarkWithTags(any())
        verify(bookmarksDao, never()).updateBookmark(any())
    }

    /**
     * Logout has to empty every table, not most of them. Tags were left behind once already; the
     * cached article text was left behind after that, and it is the worst one to leave, because
     * the offline fallback looks it up by bookmark id with nothing tying the row to an account.
     */
    @Test
    fun `logout clears every local table including the cached article text`() = runTest {
        bookmarksRepository.deleteAllLocalBookmarks()

        verify(bookmarksDao).deleteAll()
        verify(bookmarksDao).clearBookmarkTagCrossRefs()
        verify(tagDao).deleteAllTags()
        verify(bookmarkHtmlDao).deleteAll()
    }
}
