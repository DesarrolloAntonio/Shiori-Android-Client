package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Adding a bookmark writes a row and queues a sync job, and both are keyed on the same temporary
 * id. The failures below are the ones that came from generating that id more than once.
 */
class AddBookmarkUseCaseTest {

    private val dao: BookmarksDao = mock()
    private val syncManager: SyncWorks = mock()
    private val useCase = AddBookmarkUseCase(dao, syncManager)

    private fun newBookmark() = Bookmark(
        url = "https://kotlinlang.org",
        tags = listOf(Tag(id = 1, name = "kotlin")),
        public = 0,
        createArchive = false,
        createEbook = false,
        title = "Kotlin",
    )

    /**
     * The worker resolves the row by the id it was scheduled with. If that is not the id the row
     * was stored under it throws BookmarkNotFoundException and keeps retrying, and the bookmark
     * never reaches the server.
     *
     * Bookmark's secondary constructor already stamps an id, and the use case used to stamp a
     * second one. They match only while both land inside the same second.
     */
    @Test
    fun `stores the row and schedules the sync under the same id`() = runTest {
        whenever(dao.getBookmarkById(any())).thenReturn(null)

        useCase(newBookmark())

        val stored = argumentCaptor<BookmarkEntity>()
        verify(dao).insertBookmark(stored.capture())

        val scheduled = argumentCaptor<Bookmark>()
        verify(syncManager).scheduleSyncWork(eq(SyncOperationType.CREATE), scheduled.capture(), anyOrNull())

        assertEquals(
            stored.firstValue.id,
            scheduled.firstValue.id,
            "the sync job must be keyed on the id the row was actually stored under",
        )
    }

    /**
     * Ids are epoch seconds, so two bookmarks added in the same second collide. The row insert
     * replaces on conflict, so the first one is quietly overwritten, and the unique work name is
     * built from the id too, so the first one's pending sync is cancelled.
     */
    @Test
    fun `steps past an id that is already taken`() = runTest {
        val taken = (System.currentTimeMillis() / 1000).toInt()
        whenever(dao.getBookmarkById(any())).thenAnswer { invocation ->
            if (invocation.arguments[0] as Int == taken) BOOKMARK_ENTITY else null
        }

        useCase(newBookmark())

        val stored = argumentCaptor<BookmarkEntity>()
        verify(dao).insertBookmark(stored.capture())

        assertNotEquals(
            taken,
            stored.firstValue.id,
            "must not reuse an id that is already in the table",
        )
        assertTrue(
            stored.firstValue.id > 1_000_000,
            "the replacement must still read as a temporary id",
        )
    }

    private companion object {
        val BOOKMARK_ENTITY = BookmarkEntity(
            id = 1,
            url = "https://example.com",
            title = "taken",
            excerpt = "",
            author = "",
            isPublic = 0,
            createdAt = "",
            modified = "",
            imageURL = "",
            hasContent = false,
            hasArchive = false,
            hasEbook = false,
            tags = emptyList(),
            createArchive = false,
            createEbook = false,
        )
    }
}
