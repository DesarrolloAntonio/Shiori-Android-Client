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

    @Test
    fun `a bookmark the server has not scraped yet is pending`() {
        assertTrue(
            bookmark(excerpt = "", imageURL = "", hasContent = false).isPendingServerProcessing
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
