package com.desarrollodroide.domain.usecase

import com.desarrollodroide.model.serverTimestampNow
import android.os.Build
import androidx.annotation.RequiresApi
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.EditedField
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.SyncOperationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EditBookmarkUseCase(
    private val bookmarksDao: BookmarksDao,
    private val tagsDao: TagDao,
    private val syncManager: SyncWorks
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        bookmark: Bookmark,
        editedFields: Set<EditedField> = emptySet(),
    ) {
        val updatedBookmark = bookmark.copy(
            modified = serverTimestampNow()
        )
        // What the user took off in this edit, plus what an edit still waiting to upload was going
        // to take off (this one replaces its job), minus anything put back since. Only these are
        // removed on the server: a tag added there since the last sync is not the user's removal.
        val keptNames = updatedBookmark.tags.map { it.name }.toSet()
        val removedNow = bookmarksDao.getBookmarkById(bookmark.id)?.tags.orEmpty()
            .map { it.name }
            .filter { it !in keptNames }
        val removedTagNames = (syncManager.pendingTagRemovals(bookmark.id) + removedNow) - keptNames
        // [editedFields] is decided by the editor, against what it showed: a sync can rewrite the
        // row while the screen is open, and comparing with the row then would claim the other
        // client's title as the user's. A waiting edit's fields are kept, since this job replaces it.
        val allEditedFields = syncManager.pendingEditedFields(bookmark.id) + editedFields
        updatedBookmark.tags.forEach { tag ->
            tagsDao.insertTag(tag.toEntityModel())
        }
        bookmarksDao.updateBookmarkWithTags(updatedBookmark.toEntityModel())
        syncManager.scheduleSyncWork(
            SyncOperationType.UPDATE,
            updatedBookmark,
            removedTagNames = removedTagNames,
            editedFields = allEditedFields,
        )
    }
}