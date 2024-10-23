package com.desarrollodroide.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.model.UpdateCachePayload
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UpdateBookmarkCacheUseCase(
    private val bookmarksDao: BookmarksDao,
    private val syncManager: SyncWorks
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        updateCachePayload: UpdateCachePayload,
        bookmark: Bookmark
    ) {
        val updatedBookmark = bookmark.copy(
            modified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        bookmarksDao.updateBookmark(updatedBookmark.toEntityModel())
        syncManager.scheduleSyncWork(
            operationType = SyncOperationType.CACHE,
            bookmark = updatedBookmark,
            updateCachePayload = updateCachePayload
        )
    }
}