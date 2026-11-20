package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * What the add form does with what someone typed.
 *
 * Typing google.es and pressing add returned a 502 with an empty body, so the error read
 * "Error adding bookmark: " and the bookmark sat in the feed as pending while the worker retried
 * it. Verified against the live server: google.es 502s, https://google.es returns 200 for the same
 * input.
 */
class BookmarkUrlTest {

    @Test
    fun `a bare host gets https`() {
        assertEquals("https://google.es", normalizeBookmarkUrl("google.es"))
    }

    @Test
    fun `an existing scheme is left alone`() {
        assertEquals("http://example.com", normalizeBookmarkUrl("http://example.com"))
        assertEquals("https://example.com", normalizeBookmarkUrl("https://example.com"))
    }

    /**
     * Rewriting these to https would be a stranger failure than passing them through and letting
     * the server refuse them.
     */
    @Test
    fun `schemes the app will not fetch are still not rewritten`() {
        assertEquals("ftp://example.com", normalizeBookmarkUrl("ftp://example.com"))
    }

    @Test
    fun `surrounding whitespace is dropped`() {
        assertEquals("https://example.com", normalizeBookmarkUrl("  example.com  "))
    }

    @Test
    fun `an empty box stays empty rather than becoming a scheme`() {
        assertEquals("", normalizeBookmarkUrl(""))
        assertEquals("", normalizeBookmarkUrl("   "))
    }

    @Test
    fun `a path and query survive`() {
        assertEquals(
            "https://example.com/a/b?c=d",
            normalizeBookmarkUrl("example.com/a/b?c=d")
        )
    }

    @Test
    fun `plausible urls are accepted`() {
        assertTrue(isPlausibleBookmarkUrl("google.es"))
        assertTrue(isPlausibleBookmarkUrl("https://example.com/path"))
        assertTrue(isPlausibleBookmarkUrl("sub.domain.example.com"))
    }

    /**
     * The pasted-paragraph case. Someone once pasted 17KB of logcat into this field and the app
     * sent it.
     */
    @Test
    fun `text that is not a link is rejected`() {
        assertFalse(isPlausibleBookmarkUrl(""))
        assertFalse(isPlausibleBookmarkUrl("   "))
        assertFalse(isPlausibleBookmarkUrl("not a url at all"))
        assertFalse(isPlausibleBookmarkUrl("localhost"))
        assertFalse(isPlausibleBookmarkUrl(".com"))
        assertFalse(isPlausibleBookmarkUrl("example."))
        assertFalse(isPlausibleBookmarkUrl("2026-08-25 09:45:23 D SyncManager: BookmarkId 1787"))
    }

    @Test
    fun `a shared title is taken from the title extra first`() {
        assertEquals("The title", sharedBookmarkTitle("The title", "The subject", URL))
    }

    /** Firefox for Android sends the page title as the subject and no title extra at all. */
    @Test
    fun `a shared title falls back to the subject`() {
        assertEquals("The subject", sharedBookmarkTitle(null, "The subject", URL))
    }

    @Test
    fun `a blank title extra does not hide the subject`() {
        assertEquals("The subject", sharedBookmarkTitle("  ", "The subject", URL))
    }

    @Test
    fun `the url stands in when a share has no title`() {
        assertEquals(URL, sharedBookmarkTitle(null, null, URL))
        assertEquals(URL, sharedBookmarkTitle("", " ", URL))
    }

    /**
     * Google News shares "headline + link" as the text, and the whole string used to be sent to the
     * server as the url (Play review, 1.52.03, 2026-10-24). Opera shares the link alone, and that
     * worked.
     */
    @Test
    fun `a link shared together with its headline is pulled out of the text`() {
        assertEquals(URL, sharedLinkUrl("Headline of the article $URL"))
    }

    @Test
    fun `a link on its own line after the headline is pulled out of the text`() {
        assertEquals(URL, sharedLinkUrl("Headline of the article\n\n$URL"))
    }

    @Test
    fun `a share that is only a link keeps the link`() {
        assertEquals(URL, sharedLinkUrl("  $URL  "))
    }

    /** No link in it: leave it for the form and the server to judge, as before. */
    @Test
    fun `a share with no http link is passed through`() {
        assertEquals("example.com", sharedLinkUrl("example.com"))
    }

    @Test
    fun `the headline around a shared link becomes the title when no extra has one`() {
        assertEquals(
            "Headline of the article",
            sharedBookmarkTitle(null, null, URL, sharedText = "Headline of the article\n$URL"),
        )
    }

    @Test
    fun `a title extra still wins over the headline in the text`() {
        assertEquals(
            "The title",
            sharedBookmarkTitle("The title", null, URL, sharedText = "Headline of the article $URL"),
        )
    }

    private companion object {
        const val URL = "https://example.com/article"
    }
}
