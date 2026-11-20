package com.desarrollodroide.pagekeeper.ui.feed

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LibraryNarrowingTest {

    @Test
    fun `nothing narrowing it is the library`() {
        assertEquals(LibraryNarrowing("YOUR LIBRARY", null), libraryNarrowing(0, searching = false, showOnlyHiddenTag = false))
    }

    @Test
    fun `a tag filter says how many tags`() {
        assertEquals(LibraryNarrowing("FILTERED", "showing 1 tag"), libraryNarrowing(1, searching = false, showOnlyHiddenTag = false))
        assertEquals(LibraryNarrowing("FILTERED", "showing 3 tags"), libraryNarrowing(3, searching = false, showOnlyHiddenTag = false))
    }

    @Test
    fun `a search on its own is a search`() {
        assertEquals(LibraryNarrowing("SEARCH", "matching your search"), libraryNarrowing(0, searching = true, showOnlyHiddenTag = false))
    }

    @Test
    fun `tags and a search say both`() {
        assertEquals(LibraryNarrowing("FILTERED", "2 tags · matching your search"), libraryNarrowing(2, searching = true, showOnlyHiddenTag = false))
    }

    @Test
    fun `the hidden tag view says so`() {
        assertEquals(LibraryNarrowing("HIDDEN TAG", null), libraryNarrowing(0, searching = false, showOnlyHiddenTag = true))
        assertEquals(LibraryNarrowing("HIDDEN TAG", "matching your search"), libraryNarrowing(0, searching = true, showOnlyHiddenTag = true))
    }
}
