package com.desarrollodroide.pagekeeper.di

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.bitmapConfig
import coil3.request.SuccessResult
import coil3.toBitmap
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.ByteString.Companion.decodeBase64
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The image pipeline against a server that behaves like Shiori's thumbnail endpoint.
 *
 * Shiori sends `Cache-Control: no-cache, must-revalidate` with an `ETag` derived from the
 * bookmark's modified date, and answers a matching `If-None-Match` with 304. That is a deliberate
 * design: revalidation is cheap, and it is the only way a client notices that "update cache"
 * regenerated a thumbnail.
 *
 * The loader used to rewrite every response's `Cache-Control` to a year, which discarded all of
 * that. These tests hold the real production loader to the server's contract.
 */
@RunWith(AndroidJUnit4::class)
class ImageCacheRevalidationTest {

    private lateinit var server: MockWebServer
    private lateinit var cacheDir: File

    // 1x1 PNGs, distinguishable by colour so a stale image can be told from a fresh one.
    private val redPixel = requireNotNull(
        ("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQ" +
            "AAAABJRU5ErkJggg==").decodeBase64()
    )
    private val bluePixel = requireNotNull(
        ("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9aw" +
            "AAAABJRU5ErkJggg==").decodeBase64()
    )

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        cacheDir = File.createTempFile("image_cache", "").let {
            it.delete()
            it.mkdirs()
            it
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
        cacheDir.deleteRecursively()
    }

    private fun loader(): ImageLoader =
        buildImageLoader(ApplicationProvider.getApplicationContext(), cacheDir)

    private fun thumbnailResponse(etag: String, body: okio.ByteString) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "image/png")
        .setHeader("Cache-Control", "no-cache, must-revalidate")
        .setHeader("ETag", etag)
        .setHeader("Last-Modified", "2026-01-01 00:00:00")
        .setBody(Buffer().write(body))

    /**
     * The memory cache is switched off so the disk and network layers are the thing under test —
     * with it on, a second load never leaves the process and there is nothing to observe. Hardware
     * bitmaps are off for the same reason production sets an explicit config: their pixels cannot
     * be read back.
     */
    private suspend fun load(loader: ImageLoader, url: String) =
        loader.execute(
            ImageRequest.Builder(ApplicationProvider.getApplicationContext<android.content.Context>())
                .data(url)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888)
                .allowHardware(false)
                .build()
        )

    /**
     * The point of the whole thing: a second load must ask the server whether the cached copy is
     * still good, rather than assuming it is for a year.
     */
    @Test
    fun secondLoadRevalidatesWithIfNoneMatch() = runBlocking {
        val etag = "w/thumb/1-2026-01-01"
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (request.getHeader("If-None-Match") == etag) {
                    MockResponse().setResponseCode(304)
                } else {
                    thumbnailResponse(etag, redPixel)
                }
        }
        val url = server.url("/bookmark/1/thumb").toString()

        val loader = loader()
        load(loader, url)
        load(loader, url)

        assertEquals("the thumbnail should have been requested twice", 2, server.requestCount)
        val first = server.takeRequest()
        val second = server.takeRequest()
        assertNull("nothing is cached yet on the first load", first.getHeader("If-None-Match"))
        assertNotNull(
            "the second load must revalidate; a rewritten Cache-Control suppresses this entirely",
            second.getHeader("If-None-Match")
        )
        assertEquals(etag, second.getHeader("If-None-Match"))
    }

    /**
     * What the bug actually cost the user. "Update cache" regenerates the thumbnail and the ETag
     * changes with the bookmark's modified date; the app has to show the new picture.
     */
    @Test
    fun aChangedThumbnailIsPickedUp() = runBlocking {
        var currentEtag = "w/thumb/1-2026-01-01"
        var currentBody = redPixel
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (request.getHeader("If-None-Match") == currentEtag) {
                    MockResponse().setResponseCode(304)
                } else {
                    thumbnailResponse(currentEtag, currentBody)
                }
        }
        val url = server.url("/bookmark/1/thumb").toString()

        val loader = loader()
        val before = load(loader, url)
        val redPixelColor = (before as SuccessResult).image.toBitmap().getPixel(0, 0)

        // The server regenerates the thumbnail, exactly as an update cache would.
        currentEtag = "w/thumb/1-2026-06-01"
        currentBody = bluePixel

        val after = load(loader, url)
        val afterColor = (after as SuccessResult).image.toBitmap().getPixel(0, 0)

        assertEquals(
            "an updated thumbnail must reach the screen, not sit behind a year long cache entry",
            bluePixel.hashCode() != redPixel.hashCode(),
            redPixelColor != afterColor
        )
    }

    /**
     * The long lifetime the old override was reaching for is still applied, but only where the
     * server said nothing at all about caching.
     */
    @Test
    fun responsesWithNoCachingHintsStillGetALongLifetime() {
        val response = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("http://example.com/i.png").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        val result = applyFallbackCacheControl(response)

        assertEquals("public, max-age=31536000", result.header("Cache-Control"))
    }

    @Test
    fun aServersOwnCachingHeadersAreLeftAlone() {
        val response = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("http://example.com/i.png").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("Cache-Control", "no-cache, must-revalidate")
            .header("ETag", "w/thumb/1-2026-01-01")
            .build()

        val result = applyFallbackCacheControl(response)

        assertEquals("no-cache, must-revalidate", result.header("Cache-Control"))
    }
}
