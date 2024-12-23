package com.desarrollodroide.domain.usecase

import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.SyncOperationType

class AddBookmarkUseCase(
    private val bookmarksDao: BookmarksDao,
    private val syncManager: SyncWorks,
) {
    suspend operator fun invoke(
        bookmark: Bookmark
    ) {
        // Insert the bookmark locally with a timestamp as a temporary ID
        val timestampId = (System.currentTimeMillis() / 1000).toInt()
        val bookmarkWithTempId = bookmark.copy(id = timestampId)
        bookmarksDao.insertBookmark(bookmarkWithTempId.toEntityModel())
        // Schedule the sync work and wait for it to complete
        syncManager.scheduleSyncWork(SyncOperationType.CREATE, bookmark)
    }
}