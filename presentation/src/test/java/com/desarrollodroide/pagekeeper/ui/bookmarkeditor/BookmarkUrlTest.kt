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
}
