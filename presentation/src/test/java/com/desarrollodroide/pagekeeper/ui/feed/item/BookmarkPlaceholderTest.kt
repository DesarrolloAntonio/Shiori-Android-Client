package com.desarrollodroide.pagekeeper.ui.feed.item

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The stand-in a card shows when the server had no thumbnail.
 *
 * The point of deriving it from the host is that a site always looks the same, so a card is
 * recognisable before the title has been read. That only holds if these are stable.
 */
class BookmarkPlaceholderTest {

    @Test
    fun `the host is taken without scheme, path or www`() {
        assertEquals("gradle.org", hostOf("https://gradle.org/releases"))
        assertEquals("gradle.org", hostOf("http://www.gradle.org"))
        assertEquals("android-developers.googleblog.com", hostOf("https://android-developers.googleblog.com/2026/01/x.html?utm=1"))
        assertEquals("example.org", hostOf("EXAMPLE.ORG"))
    }

    /** A bookmark shared from somewhere odd should not blow up the card. */
    @Test
    fun `something that is not a url still yields something`() {
        assertEquals("", hostOf(""))
        assertEquals("not a url", hostOf("not a url"))
    }

    /**
     * The whole idea rests on this: the same site gets the same colour every time, including
     * across restarts and devices. String.hashCode is specified, unlike a default object hash.
     */
    @Test
    fun `a host always lands on the same palette entry`() {
        val first = placeholderPaletteIndex("gradle.org", 4)
        repeat(50) {
            assertEquals(first, placeholderPaletteIndex("gradle.org", 4))
        }
    }

    @Test
    fun `the index is always inside the palette`() {
        val hosts = listOf(
            "gradle.org", "example.org", "m3.material.io", "kotlinlang.org",
            "android-developers.googleblog.com", "", "a", "zzzzzzzzzzzzzzzzzzzz",
        )
        hosts.forEach { host ->
            val index = placeholderPaletteIndex(host, 4)
            assertTrue(index in 0..3, "$host produced $index")
        }
    }

    /** hashCode can be negative; a plain remainder would index backwards off the list. */
    @Test
    fun `hosts hashing negative do not produce a negative index`() {
        val negative = generateSequence(0) { it + 1 }
            .map { "host$it.example" }
            .first { it.hashCode() < 0 }
        assertTrue(placeholderPaletteIndex(negative, 4) >= 0)
    }

    @Test
    fun `an empty palette does not divide by zero`() {
        assertEquals(0, placeholderPaletteIndex("gradle.org", 0))
    }
}
