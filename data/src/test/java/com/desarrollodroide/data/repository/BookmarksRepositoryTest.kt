package com.desarrollodroide.data.repository

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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.*
import retrofit2.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.check
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.network.model.SingleBookmarkResponseDTO
import com.desarrollodroide.data.local.room.dao.BookmarkHtmlDao
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.mapper.toEditBookmarkDTO
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarkResponseDTO
import com.desarrollodroide.network.model.TagDTO
import com.desarrollodroide.network.model.BookmarksDTO
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

    @Mock
    private lateinit var syncWorks: SyncWorks

    private lateinit var bookmarksRepository: BookmarksRepositoryImpl

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        kotlinx.coroutines.runBlocking { `when`(syncWorks.bookmarkIdsWithPendingChanges()).thenReturn(emptySet()) }
        bookmarksRepository = BookmarksRepositoryImpl(apiService, bookmarksDao, tagDao, bookmarkHtmlDao, errorHandler, syncWorks)
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

    /**
     * A refresh must not write over a change still waiting to upload. Seen on a device (QA campaign,
     * process 04, #13): an edit's upload failed once, a pull to refresh wrote the server's copy over
     * the row during the retry's wait, and the retry uploaded that copy — the new tag was gone from
     * the card, the database and the server, and the job reported success.
     */
    @Test
    fun `a sync leaves a bookmark whose edit is waiting to upload as it is`() = runTest {
        `when`(syncWorks.bookmarkIdsWithPendingChanges()).thenReturn(setOf(97))
        `when`(apiService.getPagingBookmarks(anyString(), anyString())).thenReturn(Response.success(pageWith97And98))

        bookmarksRepository.syncAllBookmarks("session", "http://test.com").toList()

        verify(bookmarksDao).insertPageWithTags(check { assertEquals(listOf(98), it.map { row -> row.id }) })
    }

    /** Nor removed when the server no longer returns it: its job still has to run, and says so if it fails. */
    @Test
    fun `a sync does not prune a bookmark whose change is waiting to upload`() = runTest {
        `when`(syncWorks.bookmarkIdsWithPendingChanges()).thenReturn(setOf(97))
        `when`(apiService.getPagingBookmarks(anyString(), anyString()))
            .thenReturn(Response.success(pageWith97And98.copy(bookmarks = pageWith97And98.bookmarks!!.drop(1))))

        bookmarksRepository.syncAllBookmarks("session", "http://test.com").toList()

        verify(bookmarksDao).deleteBookmarksNotIn(check { assertTrue(97 in it, "97 pruned: $it") })
    }

    /** The pair (R7): with nothing waiting, the server's copy is written as before. */
    @Test
    fun `a sync writes every bookmark when nothing is waiting to upload`() = runTest {
        `when`(apiService.getPagingBookmarks(anyString(), anyString())).thenReturn(Response.success(pageWith97And98))

        bookmarksRepository.syncAllBookmarks("session", "http://test.com").toList()

        verify(bookmarksDao).insertPageWithTags(check { assertEquals(listOf(97, 98), it.map { row -> row.id }) })
    }

    private val pageWith97And98 = BookmarksDTO(
        page = 1, maxPage = 1, bookmarks = listOf(
            BookmarkDTO(97, "http://a.com/97", "server copy", "", "", 0, "2023-01-01", "", "", false, false, false, listOf(), false, false),
            BookmarkDTO(98, "http://a.com/98", "B", "", "", 0, "2023-01-01", "", "", false, false, false, listOf(), false, false),
        )
    )

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
        stubServerCopy(edited)
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
     * Shiori's legacy PUT /api/bookmarks adds tags but never removes one: on 1.8.0 it answers 200
     * with the removed tag still attached, with or without `deleted: true`. The edit looked saved,
     * and the next sync put the tag back. Seen on a device; measured against the server.
     */
    @Test
    fun `a tag removed in an edit is removed on the server too`() = runTest {
        val serverKeptIt = BookmarkDTO(
            89, "http://a.com", "A", "", "", 1, "2023-01-01", "2023-01-02", "",
            true, true, true, listOf(TagDTO(id = 9, name = "qa_a", nBookmarks = 0), TagDTO(id = 11, name = "qa_c", nBookmarks = 0)), true, true
        )
        stubServerCopy(serverKeptIt)
        `when`(apiService.editBookmark(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(SingleBookmarkResponseDTO(ok = true, message = serverKeptIt)))
        `when`(apiService.removeTagFromBookmark(anyString(), anyString(), anyString())).thenReturn(Response.success(Unit))

        bookmarksRepository.editBookmark(
            xSession = "session",
            serverUrl = "http://test.com",
            bookmark = serverKeptIt.copy(tags = listOf(TagDTO(id = 9, name = "qa_a", nBookmarks = 0))).toDomainModel(),
            removedTagNames = setOf("qa_c"),
        )

        verify(apiService).removeTagFromBookmark(
            eq("http://test.com/api/v1/bookmarks/89/tags"),
            eq("Bearer session"),
            eq("{\"tag_id\":11}"),
        )
        verify(apiService, never()).removeTagFromBookmark(anyString(), anyString(), eq("{\"tag_id\":9}"))
    }

    /**
     * QA 2026-09-24 M-03 (P0): a tag added on the server since this copy was synced — on the web,
     * or by the app's own "Add tags to selected" — was taken for one the user removed, and the
     * next edit of the bookmark stripped it without a word. The pair of the test above.
     */
    @Test
    fun `a tag added on the server since the last sync survives an edit that did not remove it`() = runTest {
        val serverHasMore = BookmarkDTO(
            89, "http://a.com", "A", "", "", 1, "2023-01-01", "2023-01-02", "",
            true, true, true, listOf(TagDTO(id = 9, name = "qa_a", nBookmarks = 0), TagDTO(id = 10, name = "qa_web", nBookmarks = 0)), true, true
        )
        stubServerCopy(serverHasMore)
        `when`(apiService.editBookmark(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(SingleBookmarkResponseDTO(ok = true, message = serverHasMore)))
        `when`(apiService.removeTagFromBookmark(anyString(), anyString(), anyString())).thenReturn(Response.success(Unit))

        bookmarksRepository.editBookmark(
            xSession = "session",
            serverUrl = "http://test.com",
            bookmark = serverHasMore.copy(public = 0, tags = listOf(TagDTO(id = 9, name = "qa_a", nBookmarks = 0))).toDomainModel(),
        )

        verify(apiService, never()).removeTagFromBookmark(anyString(), anyString(), anyString())
        verify(apiService, never()).addTagsToBookmarks(anyString(), anyString(), anyString())
        verify(bookmarksDao).updateBookmarkWithTags(check { assertEquals(listOf("qa_a", "qa_web"), it.tags.map { tag -> tag.name }) })
    }

    /**
     * QA 2026-09-24 M-02 (P1): the removal went through the bulk route, which answers 400 "tag_ids
     * should not be empty" when no tag is left, so taking off a bookmark's last tag never reached
     * the server: five retries, then FAILED.
     */
    @Test
    fun `removing a bookmark's last tag is sent as a removal of that tag`() = runTest {
        val serverCopy = BookmarkDTO(
            89, "http://a.com", "A", "", "", 1, "2023-01-01", "2023-01-02", "",
            true, true, true, listOf(TagDTO(id = 9, name = "qa_a", nBookmarks = 0)), true, true
        )
        stubServerCopy(serverCopy)
        `when`(apiService.editBookmark(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(SingleBookmarkResponseDTO(ok = true, message = serverCopy)))
        `when`(apiService.removeTagFromBookmark(anyString(), anyString(), anyString())).thenReturn(Response.success(Unit))

        bookmarksRepository.editBookmark(
            xSession = "session",
            serverUrl = "http://test.com",
            bookmark = serverCopy.copy(tags = emptyList()).toDomainModel(),
            removedTagNames = setOf("qa_a"),
        )

        verify(apiService).removeTagFromBookmark(eq("http://test.com/api/v1/bookmarks/89/tags"), eq("Bearer session"), eq("{\"tag_id\":9}"))
        verify(apiService, never()).addTagsToBookmarks(anyString(), anyString(), anyString())
        verify(bookmarksDao).updateBookmarkWithTags(check { assertTrue(it.tags.isEmpty(), "${it.tags}") })
    }

    /** The pair (R7): when the server already has exactly the tags asked for, nothing else is sent. */
    @Test
    fun `an edit the server applied in full sends nothing more`() = runTest {
        val applied = BookmarkDTO(
            89, "http://a.com", "A", "", "", 1, "2023-01-01", "2023-01-02", "",
            true, true, true, listOf(TagDTO(id = 9, name = "qa_a", nBookmarks = 0)), true, true
        )
        stubServerCopy(applied)
        `when`(apiService.editBookmark(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(SingleBookmarkResponseDTO(ok = true, message = applied)))

        bookmarksRepository.editBookmark(xSession = "session", serverUrl = "http://test.com", bookmark = applied.toDomainModel())

        verify(apiService, never()).addTagsToBookmarks(anyString(), anyString(), anyString())
        verify(apiService, never()).removeTagFromBookmark(anyString(), anyString(), anyString())
    }

    /**
     * QA 2026-09-24 M-03: Shiori 1.8.0 answers the bulk route with `"message": null`, so nothing
     * came back to store, the card never showed the new tag, and the next edit uploaded the old set.
     */
    @Test
    fun `tags added to a selection reach Room when the server sends no bookmarks back`() = runTest {
        `when`(apiService.addTagsToBookmarks(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(BookmarkResponseDTO(ok = true, message = null)))
        `when`(tagDao.getAllTags()).thenReturn(flowOf(listOf(
            com.desarrollodroide.data.local.room.entity.TagEntity(9, "qa_a", 0),
            com.desarrollodroide.data.local.room.entity.TagEntity(10, "qa_b", 0),
        )))
        `when`(bookmarksDao.getBookmarkById(89)).thenReturn(
            BookmarkEntity(89, "http://a.com", "A", "", "", 1, "2023-01-01", "2023-01-02", "", true, true, true,
                listOf(com.desarrollodroide.model.Tag(id = 9, name = "qa_a")), true, true)
        )

        bookmarksRepository.addTagsToBookmarks(token = "t", serverUrl = "http://test.com", bookmarkIds = listOf(89), tagIds = listOf(9, 10))

        verify(bookmarksDao).updateBookmarkWithTags(check {
            assertEquals(listOf("qa_a", "qa_b"), it.tags.map { tag -> tag.name })
            assertEquals(1, it.isPublic)
        })
    }

    /**
     * An edit made offline is uploaded later, and the server's PUT overwrites url, title and
     * excerpt with whatever it is sent. Sending the row as this device last saw it undid anything
     * another client had changed meanwhile: an excerpt edited on the web came back as the old one
     * once the app's tag edit drained (QA campaign, process 04, C1).
     */
    @Test
    fun `an uploaded edit keeps what another client changed on the server`() = runTest {
        stubServerCopy(serverSideCopy)
        stubEditAnswer()

        bookmarksRepository.editBookmark(xSession = "session", serverUrl = "http://test.com", bookmark = offlineEdit)

        verify(apiService).editBookmark(anyString(), anyString(), check { json ->
            val sent = com.google.gson.JsonParser.parseString(json).asJsonObject
            assertEquals("QA server-side edit", sent.get("excerpt").asString)
            assertEquals("QA_Off01 renamed on the web", sent.get("title").asString)
        })
    }

    /** The pair (R7): what the app did edit — tags and Public — is sent as edited, not as the server had it. */
    @Test
    fun `an uploaded edit still sends the tags and public flag the user chose`() = runTest {
        stubServerCopy(serverSideCopy)
        stubEditAnswer()

        bookmarksRepository.editBookmark(xSession = "session", serverUrl = "http://test.com", bookmark = offlineEdit)

        verify(apiService).editBookmark(anyString(), anyString(), check { json ->
            val sent = com.google.gson.JsonParser.parseString(json).asJsonObject
            assertEquals(1, sent.get("public").asInt)
            assertEquals(listOf("qa_c1"), sent.getAsJsonArray("tags").map { it.asJsonObject.get("name").asString })
        })
    }

    /** Without the server's copy there is nothing safe to send: the old row would overwrite whatever is there. */
    @Test
    fun `an edit to a bookmark the server no longer has is not sent`() = runTest {
        `when`(apiService.getPagingBookmarks(anyString(), anyString()))
            .thenReturn(Response.success(BookmarksDTO(page = 1, maxPage = 1, bookmarks = emptyList())))
        stubEditAnswer()

        val error = runCatching {
            bookmarksRepository.editBookmark(xSession = "session", serverUrl = "http://test.com", bookmark = offlineEdit)
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException, "expected the upload to fail, got $error")
        verify(apiService, never()).editBookmark(anyString(), anyString(), anyString())
    }

    /** The worker renews an expired session by reading the server's message, so the read has to pass it on. */
    @Test
    fun `an expired session while reading the server copy is reported as such`() = runTest {
        `when`(apiService.getPagingBookmarks(anyString(), anyString()))
            .thenReturn(Response.error(401, SESSION_HAS_BEEN_EXPIRED.toResponseBody("text/plain".toMediaTypeOrNull())))

        val error = runCatching {
            bookmarksRepository.editBookmark(xSession = "session", serverUrl = "http://test.com", bookmark = offlineEdit)
        }.exceptionOrNull()

        assertTrue(error?.message?.contains(SESSION_HAS_BEEN_EXPIRED) == true, "got $error")
        verify(apiService, never()).editBookmark(anyString(), anyString(), anyString())
    }

    private val serverSideCopy = BookmarkDTO(
        97, "http://qa.example/QA_Off01", "QA_Off01 renamed on the web", "QA server-side edit", "", 0,
        "2023-01-01", "2023-01-03", "", true, false, false, listOf(TagDTO(id = 3, name = "qa_old", nBookmarks = 1)), false, false
    )

    private val offlineEdit = serverSideCopy.copy(
        title = "QA_Off01", excerpt = "offline fixture", public = 1,
        tags = listOf(TagDTO(id = null, name = "qa_c1", nBookmarks = null)),
    ).toDomainModel()

    private suspend fun stubServerCopy(copy: BookmarkDTO) {
        `when`(apiService.getPagingBookmarks(anyString(), anyString()))
            .thenReturn(Response.success(BookmarksDTO(page = 1, maxPage = 1, bookmarks = listOf(copy.copy(id = 98, url = copy.url + "/other"), copy))))
    }

    private suspend fun stubEditAnswer() {
        `when`(apiService.editBookmark(anyString(), anyString(), anyString()))
            .thenReturn(Response.success(SingleBookmarkResponseDTO(ok = true, message = offlineEdit.toEditBookmarkDTO())))
    }

    /**
     * Refreshing one bookmark asks for that one url, not for every page.
     *
     * The pending banner used to say pull to refresh, which walks the whole library: on ten
     * thousand bookmarks, 334 requests to find out about one card.
     */
    @Test
    fun `refreshing a bookmark asks the server for just that url`() = runTest {
        val fresh = BookmarkDTO(
            7, "http://a.com/x", "Scraped title", "an excerpt", "", 1, "2023-01-01", "2023-01-02",
            "/thumb/7", true, true, true, listOf(), true, true
        )
        val other = fresh.copy(id = 8, url = "http://a.com/x/other")
        `when`(apiService.getPagingBookmarks(anyString(), anyString()))
            .thenReturn(Response.success(BookmarksDTO(page = 1, maxPage = 1, bookmarks = listOf(other, fresh))))

        val result = bookmarksRepository.refreshBookmark(
            xSession = "session",
            serverUrl = "http://test.com",
            bookmark = fresh.toDomainModel().copy(id = 1_787_646_812),
        )

        assertEquals(7, result?.id)
        // keyword is a substring match, so the response can carry more than the one asked for.
        verify(apiService).getPagingBookmarks(
            anyString(),
            check { url -> assertTrue(url.contains("keyword=")) },
        )
        // The local row carried a temporary id; an update keyed on it would have matched nothing.
        verify(bookmarksDao).deleteBookmarkById(1_787_646_812)
        verify(bookmarksDao).insertPageWithTags(anyList())
    }

    /** A url the server does not know about is not an error, it just has nothing to say. */
    @Test
    fun `refreshing a bookmark the server does not have returns null`() = runTest {
        `when`(apiService.getPagingBookmarks(anyString(), anyString()))
            .thenReturn(Response.success(BookmarksDTO(page = 1, maxPage = 1, bookmarks = emptyList())))

        val result = bookmarksRepository.refreshBookmark(
            xSession = "session",
            serverUrl = "http://test.com",
            bookmark = BookmarkDTO(
                1, "http://gone.example", "", "", "", 1, "", "", "",
                false, false, false, listOf(), false, false
            ).toDomainModel(),
        )

        assertNull(result)
        verify(bookmarksDao, never()).insertPageWithTags(anyList())
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
