package com.desarrollodroide.network.retrofit

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.SocketTimeoutException

/** What older servers answered to a dead session, and what the rest of the app looks for. */
const val SESSION_EXPIRED_MESSAGE = "session has been expired"

/**
 * Turns Shiori 1.8.0's ways of refusing a token into the one answer the app understands.
 *
 * Everything that reacts to an expired session looks for [SESSION_EXPIRED_MESSAGE], and 1.8.0 no
 * longer says it. Its legacy routes (`/api/bookmarks`, `/api/tags`) panic on a bad token and close
 * the connection without a response, and its v1 routes answer 401 or 403 with "Authentication
 * required". So a dead session read as a network failure: the feed said the server could not be
 * reached, nothing offered a login, and queued changes were retried until they gave up.
 *
 * A dropped connection proves nothing by itself, since a proxy or a restarting server looks the
 * same, so the token is put to `/api/v1/auth/me`, which does answer. Only a refusal from there
 * counts. Anything else is passed on as the failure it was.
 */
class ExpiredSessionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            // Which exception arrives says little: on a server with several addresses OkHttp tries
            // the next one after the drop and reports how that went. Timeouts are skipped only
            // because the check would leave someone waiting another 30 seconds for nothing.
            if (request.isLegacyApiCall() && e !is SocketTimeoutException && chain.refusesToken(request)) {
                return request.sessionExpired(HTTP_UNAUTHORIZED)
            }
            throw e
        }
        if (request.isSessionCheckedV1Call() && response.refusesAuthentication()) {
            response.close()
            return request.sessionExpired(response.code)
        }
        return response
    }

    private fun Request.isLegacyApiCall(): Boolean =
        url.encodedPath.contains(API_PREFIX) && !url.encodedPath.contains(V1_PREFIX)

    /** The login route refuses too, but what it is refusing is a password. */
    private fun Request.isSessionCheckedV1Call(): Boolean =
        url.encodedPath.contains(V1_PREFIX) && !url.encodedPath.endsWith(LOGIN_PATH)


    private fun Interceptor.Chain.refusesToken(original: Request): Boolean {
        val serverRoot = original.url.encodedPath.substringBefore(API_PREFIX)
        val check = Request.Builder()
            .url(original.url.newBuilder().encodedPath(serverRoot + ME_PATH).query(null).build())
            .apply { original.header(AUTHORIZATION)?.let { header(AUTHORIZATION, it) } }
            .build()
        return try {
            proceed(check).use { it.code == HTTP_UNAUTHORIZED || it.code == HTTP_FORBIDDEN }
        } catch (_: IOException) {
            false
        }
    }

    private fun Response.refusesAuthentication(): Boolean =
        (code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN) &&
            peekBody(BODY_PEEK_BYTES).string().contains(AUTHENTICATION_REQUIRED)

    private fun Request.sessionExpired(code: Int): Response = Response.Builder()
        .request(this)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(SESSION_EXPIRED_MESSAGE)
        .body(SESSION_EXPIRED_MESSAGE.toResponseBody(PLAIN_TEXT))
        .build()

    private companion object {
        const val API_PREFIX = "/api/"
        const val V1_PREFIX = "/api/v1/"
        const val ME_PATH = "/api/v1/auth/me"
        const val LOGIN_PATH = "/auth/login"
        const val AUTHORIZATION = "Authorization"
        const val AUTHENTICATION_REQUIRED = "Authentication required"
        const val BODY_PEEK_BYTES = 1024L
        val PLAIN_TEXT = "text/plain".toMediaType()
    }
}
