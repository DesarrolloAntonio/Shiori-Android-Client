package com.desarrollodroide.data.repository.workers

import com.desarrollodroide.data.repository.syncJobLabel
import com.desarrollodroide.model.Bookmark
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SyncJobLabelTest {

    @Test
    fun `a link added by hand has no title yet, so its job is named by its url`() {
        val bookmark = Bookmark.mock().copy(title = "", url = "https://example.com/?a=1")
        assertEquals("https://example.com/?a=1", syncJobLabel(bookmark))
    }

    /** The pair (R7): a bookmark that has a title keeps it. */
    @Test
    fun `a bookmark with a title is named by its title`() {
        val bookmark = Bookmark.mock().copy(title = "Effective Go", url = "https://go.dev/doc/effective_go")
        assertEquals("Effective Go", syncJobLabel(bookmark))
    }
}
