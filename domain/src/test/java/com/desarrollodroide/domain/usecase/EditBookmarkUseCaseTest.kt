package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * QA 2026-09-24 M-03: the upload may only remove the tags the user took off. Anything else the
 * server has was added there since the last sync, and stripping it lost it for good.
 */
class EditBookmarkUseCaseTest {

    private val bookmarksDao: BookmarksDao = mock()
    private val tagsDao: TagDao = mock()
    private val syncManager: SyncWorks = mock()
    private val useCase = EditBookmarkUseCase(bookmarksDao, tagsDao, syncManager)

    private fun stored(vararg tags: String) = BookmarkEntity(
        89, "http://a.com", "A", "", "", 0, "2023-01-01", "2023-01-02", "", true, false, false,
        tags.mapIndexed { i, name -> Tag(id = i + 1, name = name) }, false, false,
    )

    private fun edited(vararg tags: String) = Bookmark(
        id = 89, url = "http://a.com", title = "A", excerpt = "", author = "", public = 1,
        createAt = "2023-01-01", modified = "2023-01-02", imageURL = "", hasContent = true,
        hasArchive = false, hasEbook = false, tags = tags.mapIndexed { i, name -> Tag(id = i + 1, name = name) },
        createArchive = false, createEbook = false,
    )

    private suspend fun scheduledRemovals(): Set<String> {
        val removed = argumentCaptor<Set<String>>()
        verify(syncManager).scheduleSyncWork(eq(SyncOperationType.UPDATE), any(), anyOrNull(), removed.capture())
        return removed.firstValue
    }

    @Test
    fun `the job removes what the user took off and what a waiting edit was going to take off`() = runTest {
        whenever(bookmarksDao.getBookmarkById(89)).thenReturn(stored("qa_a", "qa_b"))
        whenever(syncManager.pendingTagRemovals(89)).thenReturn(setOf("qa_old"))

        useCase(edited("qa_a"))

        assertEquals(setOf("qa_b", "qa_old"), scheduledRemovals())
    }

    /** The pair (R7): a tag kept, or put back after an earlier edit removed it, is not removed. */
    @Test
    fun `a tag kept or put back is not removed`() = runTest {
        whenever(bookmarksDao.getBookmarkById(89)).thenReturn(stored("qa_a"))
        whenever(syncManager.pendingTagRemovals(89)).thenReturn(setOf("qa_old"))

        useCase(edited("qa_a", "qa_old"))

        assertEquals(emptySet<String>(), scheduledRemovals())
    }
}
