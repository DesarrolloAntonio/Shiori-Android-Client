package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.repository.SyncManager
import com.desarrollodroide.data.repository.SyncOperationType

class DeleteBookmarkUseCase(
    private val bookmarksDao: BookmarksDao,
    private val syncManager: SyncManager
) {
    suspend operator fun invoke(bookmarkId: Int) {
        bookmarksDao.deleteBookmarkById(bookmarkId)
        syncManager.scheduleSyncWork(SyncOperationType.DELETE, bookmarkId)
    }
}