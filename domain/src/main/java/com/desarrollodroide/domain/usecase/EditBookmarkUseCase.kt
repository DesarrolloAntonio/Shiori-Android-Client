package com.desarrollodroide.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.desarrollodroide.model.Bookmark
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
        bookmark: Bookmark
    ) {
        val updatedBookmark = bookmark.copy(
            modified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        updatedBookmark.tags.forEach { tag ->
            tagsDao.insertTag(tag.toEntityModel())
        }
        bookmarksDao.updateBookmarkWithTags(updatedBookmark.toEntityModel())
        syncManager.scheduleSyncWork(SyncOperationType.UPDATE, updatedBookmark)
    }
}