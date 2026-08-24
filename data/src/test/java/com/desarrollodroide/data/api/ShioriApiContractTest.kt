package com.desarrollodroide.data.api

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.repository.AuthRepositoryImpl
import com.desarrollodroide.data.repository.TagsRepositoryImpl
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Drives the real Retrofit stack against canned Shiori responses.
 *
 * The payloads here are not invented. They match what the server actually emits:
 * MessageResponseMiddleware wraps every JSON body as {"ok": statusCode < 400, "message": ...}
 * (internal/http/middleware/message_response.go), and model.TagDTO serializes as
 * {id, name, bookmark_count, deleted} (internal/model/tag.go). A DELETE answers 204 with no body
 * at all, because the middleware only wraps responses whose Content-Type is application/json.
 *
 * This is the layer that a manual poke at a live server would exercise, except it also pins the
 * request side: path, method, auth header and body shape.
 */
@ExperimentalCoroutinesApi
class ShioriApiContractTest {

    private lateinit var server: MockWebServer
    private lateinit var api: RetrofitNetwork

    @Mock
    private lateinit var tagsDao: TagDao

    @Mock
    private lateinit var prefs: SettingsPreferenceDataSource

    @Mock
    private lateinit var errorHandler: ErrorHandler

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitNetwork::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun baseUrl() = server.url("/").toString().trimEnd('/')

    private fun tagsRepo() = TagsRepositoryImpl(api, tagsDao, errorHandler)
    private fun authRepo() = AuthRepositoryImpl(api, prefs, errorHandler)

    // --- tags -------------------------------------------------------------------------------

    @Test
    fun `listing tags parses the wrapped array and asks for bookmark counts`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """{"ok":true,"message":[
                        {"id":1,"name":"android","bookmark_count":3,"deleted":false},
                        {"id":2,"name":"test2","bookmark_count":0,"deleted":false}
                    ]}"""
                )
        )
        `when`(tagsDao.getAllTags()).thenReturn(flowOf(emptyList()))

        tagsRepo().getTags("tok", baseUrl()).toList()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/tags?with_bookmark_count=true", request.path)
        assertEquals("Bearer tok", request.getHeader("Authorization"))
    }

    @Test
    fun `creating a tag posts only a name and reads back the created tag`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"ok":true,"message":{"id":9,"name":"kotlin","bookmark_count":0,"deleted":false}}""")
        )

        val results = tagsRepo().createTag("tok", baseUrl(), "  kotlin  ").toList()

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/tags", request.path)
        assertEquals("Bearer tok", request.getHeader("Authorization"))
        assertTrue(request.getHeader("Content-Type").orEmpty().startsWith("application/json"))
        // Only "name" goes out. TagDTO would have serialized its count as nBookmarks, which the
        // server does not accept, which is why writes use a dedicated payload type.
        assertEquals("""{"name":"kotlin"}""", request.body.readUtf8())

        assertTrue(results.last() is Result.Success)
        assertEquals("kotlin", results.last().data?.name)
        assertEquals(9, results.last().data?.id)
    }

    @Test
    fun `renaming a tag puts to the id route`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"ok":true,"message":{"id":3,"name":"android","bookmark_count":0,"deleted":false}}""")
        )

        val results = tagsRepo().renameTag("tok", baseUrl(), 3, "android").toList()

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/tags/3", request.path)
        assertEquals("""{"name":"android"}""", request.body.readUtf8())
        assertTrue(results.last() is Result.Success)
    }

    @Test
    fun `deleting a tag copes with the empty 204 the server actually sends`() = runTest {
        // 204 with no body and no Content-Type: the middleware does not wrap it, so there is
        // nothing to deserialize. This is what Response<Unit> is for.
        server.enqueue(MockResponse().setResponseCode(204))

        val results = tagsRepo().deleteTag("tok", baseUrl(), 5).toList()

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/v1/tags/5", request.path)
        assertTrue(results.last() is Result.Success, "204 must be treated as success")
    }

    @Test
    fun `a rejected tag delete surfaces as an error and is not cached`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"ok":false,"message":"Tag not found"}""")
        )
        `when`(errorHandler.getApiError(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.ErrorType.HttpError(statusCode = 404))

        val results = tagsRepo().deleteTag("tok", baseUrl(), 5).toList()

        assertTrue(results.last() is Result.Error)
    }

    // --- auth -------------------------------------------------------------------------------

    @Test
    fun `refresh sends the bearer token and stores the new one`() = runTest {
        server.enqueue(
            MockResponse()
                // The handler answers 202 Accepted, not 200. Anything 2xx must be accepted.
                .setResponseCode(202)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"ok":true,"message":{"token":"newToken","expires":1790000000}}""")
        )

        val results = authRepo().refreshToken(baseUrl(), "oldToken").toList()

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/auth/refresh", request.path)
        assertEquals("Bearer oldToken", request.getHeader("Authorization"))
        assertTrue(results.last() is Result.Success)
        assertEquals("newToken", results.last().data)
    }

    @Test
    fun `logout uses the v1 route when the server has it`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("""{"ok":true,"message":null}""")
        )
        `when`(prefs.getUser()).thenReturn(flowOf(com.desarrollodroide.model.User("")))

        authRepo().sendLogout(baseUrl(), "sess").toList()

        assertEquals("/api/v1/auth/logout", server.takeRequest().path)
        assertNull(server.takeRequest(1, java.util.concurrent.TimeUnit.SECONDS), "must not call the legacy route")
    }

    @Test
    fun `logout falls back to the legacy route on a 1 dot 7 server`() = runTest {
        // 1.7 has no v1 logout route and answers 404 there, but the legacy one works.
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("""{"ok":true,"message":"Logout success"}""")
        )
        `when`(prefs.getUser()).thenReturn(flowOf(com.desarrollodroide.model.User("")))

        authRepo().sendLogout(baseUrl(), "sess").toList()

        assertEquals("/api/v1/auth/logout", server.takeRequest().path)
        assertEquals("/api/logout", server.takeRequest().path)
    }

    // --- bookmarks --------------------------------------------------------------------------

    @Test
    fun `update cache sends the snake_case flags the v1 endpoint reads`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"ok":true,"message":[]}""")
        )

        com.desarrollodroide.data.repository.BookmarksRepositoryImpl(
            apiService = api,
            bookmarksDao = org.mockito.Mockito.mock(com.desarrollodroide.data.local.room.dao.BookmarksDao::class.java),
            tagDao = org.mockito.Mockito.mock(com.desarrollodroide.data.local.room.dao.TagDao::class.java),
            bookmarkHtmlDao = org.mockito.Mockito.mock(com.desarrollodroide.data.local.room.dao.BookmarkHtmlDao::class.java),
            errorHandler = errorHandler,
        ).updateBookmarkCacheV1(
            token = "tok",
            serverUrl = baseUrl(),
            updateCachePayload = com.desarrollodroide.model.UpdateCachePayload(
                createArchive = true,
                createEbook = true,
                ids = listOf(7),
                keepMetadata = true,
                skipExist = true,
            ),
            bookmark = null,
        )

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/bookmarks/cache", request.path)
        val body = request.body.readUtf8()
        // Go silently drops unknown fields, so a camelCase key here is not an error, it is a
        // flag that never arrives and defaults to false.
        for (key in listOf("create_archive", "create_ebook", "keep_metadata", "skip_exist")) {
            assertTrue(body.contains("\"$key\":true"), "expected $key=true in body, got: $body")
        }
        for (key in listOf("createArchive", "createEbook", "keepMetadata", "skipExist")) {
            assertTrue(!body.contains(key), "camelCase key $key must not be sent, got: $body")
        }
    }
}
