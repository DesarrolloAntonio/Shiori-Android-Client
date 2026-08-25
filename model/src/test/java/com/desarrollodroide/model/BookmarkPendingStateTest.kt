package com.desarrollodroide.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The banner this drives tells the user to pull to refresh, so a false positive is a message that
 * never goes away no matter what they do.
 */
class BookmarkPendingStateTest {

    private fun bookmark(
        id: Int = 1,
        title: String = "A title",
        excerpt: String = "An excerpt",
        imageURL: String = "/thumb/1",
        hasContent: Boolean = true,
    ) = Bookmark.mock().copy(
        id = id,
        title = title,
        excerpt = excerpt,
        imageURL = imageURL,
        hasContent = hasContent,
    )

    @Test
    fun `a locally created bookmark is pending until the server gives it an id`() {
        assertTrue(bookmark(id = 1_756_000_000).isPendingServerProcessing)
    }

    /**
     * A bookmark with nothing on it reads as pending, whether the server is still working or
     * never will be. The two cannot be told apart from the data — Shiori does not touch
     * modified_at when it scrapes, 79 of 82 scraped bookmarks on a live server still had it equal
     * to created_at — so the flag says what is true, that there is nothing to show, and the
     * screen's Check is what settles which of the two it is.
     */
    @Test
    fun `a bookmark with nothing on it is pending`() {
        assertTrue(
            bookmark(
                title = "https://aaaa.pd",
                excerpt = "",
                imageURL = "",
                hasContent = false,
            ).isPendingServerProcessing
        )
    }

    @Test
    fun `a fully processed bookmark is not pending`() {
        assertFalse(bookmark().isPendingServerProcessing)
    }

    @Test
    fun `a title that happens to be a url is not pending`() {
        // The server stores title = url permanently when a page has no readable title. Treating
        // that as pending left the banner up forever and refreshing never cleared it.
        assertFalse(
            bookmark(title = "https://m3.material.io/", excerpt = "").isPendingServerProcessing
        )
    }
}
