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
import org.mockito.kotlin.eq
import org.mockito.kotlin.check
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.repository.paging.BookmarkPagingSource
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
    private lateinit var errorHandler: ErrorHandler

    private lateinit var bookmarksRepository: BookmarksRepositoryImpl

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        bookmarksRepository = BookmarksRepositoryImpl(apiService, bookmarksDao, errorHandler)
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

        verify(bookmarksDao).deleteAll()
        verify(bookmarksDao).insertAll(bookmarkEntities)
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
    fun `getPagingBookmarks should return paginated data when API call is successful`() = runTest {
        // Arrange
        val xSessionId = "testSessionId"
        val serverUrl = "http://test.com"
        val searchText = "test"
        val tags = listOf<Tag>()
        val saveToLocal = true
        val bookmarksDTO = BookmarksDTO(
            maxPage = 1,
            page = 1,
            bookmarks = listOf(
                BookmarkDTO(1, "http://bookmark1.com", "Bookmark 1", "Excerpt 1", "Author 1", 1, "2023-01-01", "", "http://image1.com", true, true, true, listOf(), true, true),
                BookmarkDTO(2, "http://bookmark2.com", "Bookmark 2", "Excerpt 2", "Author 2", 1, "2023-01-02", "", "http://image2.com", true, true, true, listOf(), true, true)
            )
        )
        val expectedBookmarks = bookmarksDTO.bookmarks?.map { it.toDomainModel() }

        `when`(apiService.getPagingBookmarks(eq(xSessionId), anyString())).thenReturn(Response.success(bookmarksDTO))
        `when`(bookmarksDao.getAll()).thenReturn(flowOf(emptyList()))

        // Act
        val pagingSource = BookmarkPagingSource(
            remoteDataSource = apiService,
            bookmarksDao = bookmarksDao,
            serverUrl = serverUrl,
            xSessionId = xSessionId,
            searchText = searchText,
            tags = tags,
            saveToLocal = saveToLocal
        )

        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // Assert
        assertTrue(loadResult is PagingSource.LoadResult.Page)
        loadResult as PagingSource.LoadResult.Page
        assertEquals(expectedBookmarks, loadResult.data)
    }
}