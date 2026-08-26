package com.desarrollodroide.network.retrofit

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * These logs are shown on the debug screens and can be shared from there, so what ends up in the
 * list matters as much as what ends up in logcat.
 */
class NetworkLoggerInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var interceptor: NetworkLoggerInterceptor
    private lateinit var client: OkHttpClient

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
        interceptor = NetworkLoggerInterceptor()
        client = OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @AfterEach
    fun tearDown() = server.shutdown()

    private fun call(path: String = "/") =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute().close()

    private fun loggedText() = interceptor.logs.value.joinToString("\n") { it.message }

    @Test
    fun `a token in a response body is masked before it is stored`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(TOKEN_BODY)
        )

        call()

        val logged = loggedText()
        assertFalse(logged.contains("super-secret-jwt"), "token must not be stored verbatim")
        assertFalse(logged.contains("sess-abc"), "session must not be stored verbatim")
        assertTrue(logged.contains(MASKED_TOKEN), "expected a masked token, got: " + logged)
    }

    @Test
    fun `a binary body is described rather than decoded as text`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/epub+zip")
                .setBody("PKbinary-garbage")
        )

        call("/bookmark/1/ebook")

        val logged = loggedText()
        assertTrue(logged.contains("not logged"), "binary body should be described, got: " + logged)
        assertFalse(logged.contains("binary-garbage"))
    }

    @Test
    fun `the buffer stops growing instead of running for the life of the process`() {
        repeat(250) {
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("{}"))
            call("/n" + it)
        }

        // two entries per call, request and response, capped at the most recent 200
        assertEquals(200, interceptor.logs.value.size)
    }

    private companion object {
        const val TOKEN_BODY =
            "{\"token\":\"super-secret-jwt\",\"session\":\"sess-abc\"}"
        const val MASKED_TOKEN = "\"token\":\"***\""
    }
}
