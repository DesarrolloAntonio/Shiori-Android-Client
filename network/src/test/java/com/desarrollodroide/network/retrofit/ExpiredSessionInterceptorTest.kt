package com.desarrollodroide.network.retrofit

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

/**
 * The answers here are the ones a real Shiori 1.8.0 gave to a bad token: the legacy routes close
 * the connection without a response, the v1 routes say "Authentication required".
 */
class ExpiredSessionInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private val requests = mutableListOf<RecordedRequest>()

    private var apiAnswer = MockResponse().setBody("[]")
    private var meAnswer = refused(401)

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                synchronized(requests) { requests.add(request) }
                return if (request.path.orEmpty().endsWith(ME_PATH)) meAnswer else apiAnswer
            }
        }
        server.start()
        client = OkHttpClient.Builder().addInterceptor(ExpiredSessionInterceptor()).build()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `a dropped connection with a refused token reads as an expired session`() {
        apiAnswer = dropped()

        call("/api/bookmarks?page=1").use { response ->
            assertEquals(401, response.code)
            assertEquals(SESSION_EXPIRED_MESSAGE, response.body!!.string())
        }
    }

    /** A POST is what a shared link sends, and the only kind that has a body left unread. */
    @Test
    fun `a dropped post with a refused token reads as an expired session`() {
        apiAnswer = dropped()
        val request = request("/api/bookmarks")
            .post("""{"url":"https://example.com"}""".toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(SESSION_EXPIRED_MESSAGE, response.body!!.string())
        }
    }

    /** The token is fine, so something else is wrong and the user must not be sent to log in. */
    @Test
    fun `a dropped connection with an accepted token stays a failure`() {
        apiAnswer = dropped()
        meAnswer = MockResponse().setBody("""{"ok":true}""")

        assertThrows<IOException> { call("/api/bookmarks") }
    }

    @Test
    fun `a dropped connection the check cannot answer stays a failure`() {
        apiAnswer = dropped()
        meAnswer = dropped()

        assertThrows<IOException> { call("/api/bookmarks") }
    }

    @Test
    fun `the check carries the same token and keeps the server's sub path`() {
        apiAnswer = dropped()

        call("/shiori/api/tags").close()

        val check = synchronized(requests) { requests.last() }
        assertEquals("/shiori$ME_PATH", check.path)
        assertEquals("GET", check.method)
        assertEquals("Bearer the-token", check.getHeader("Authorization"))
    }

    @Test
    fun `a v1 route refusing the token reads as an expired session`() {
        listOf(401, 403).forEach { code ->
            apiAnswer = refused(code)

            call("/api/v1/bookmarks/1/readable").use { response ->
                assertEquals(code, response.code)
                assertEquals(SESSION_EXPIRED_MESSAGE, response.body!!.string())
            }
        }
    }

    /** A wrong password is not an expired session. */
    @Test
    fun `a refused login is left alone`() {
        apiAnswer = refused(401)

        call("/api/v1/auth/login").use { response ->
            assertEquals(REFUSAL_BODY, response.body!!.string())
        }
    }

    @Test
    fun `a v1 refusal for another reason is left alone`() {
        val body = """{"ok":false,"message":{"error":"Admin access required"}}"""
        apiAnswer = MockResponse().setResponseCode(403).setBody(body)

        call("/api/v1/accounts").use { response ->
            assertEquals(body, response.body!!.string())
        }
    }

    @Test
    fun `a normal answer costs no extra request`() {
        call("/api/bookmarks").use { response ->
            assertEquals("[]", response.body!!.string())
        }

        assertEquals(1, synchronized(requests) { requests.size })
    }

    private fun call(path: String): Response = client.newCall(request(path).build()).execute()

    private fun request(path: String) = Request.Builder()
        .url(server.url(path))
        .header("Authorization", "Bearer the-token")

    private fun dropped() = MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)

    private fun refused(code: Int) = MockResponse().setResponseCode(code).setBody(REFUSAL_BODY)

    private companion object {
        const val ME_PATH = "/api/v1/auth/me"
        const val REFUSAL_BODY = """{"ok":false,"message":{"error":"Authentication required"}}"""
        val JSON = "application/json".toMediaType()
    }
}
