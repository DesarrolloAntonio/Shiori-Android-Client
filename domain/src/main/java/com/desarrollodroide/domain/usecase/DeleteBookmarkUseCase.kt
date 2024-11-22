package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.SyncOperationType

class DeleteBookmarkUseCase(
    private val bookmarksDao: BookmarksDao,
    private val syncManager: SyncWorks
) {
    suspend operator fun invoke(bookmark: Bookmark) {
        bookmarksDao.deleteBookmarkById(bookmark.id)
        syncManager.scheduleSyncWork(SyncOperationType.DELETE, bookmark)
    }
}