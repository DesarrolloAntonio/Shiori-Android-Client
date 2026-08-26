package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.SyncOperationType

class AddBookmarkUseCase(
    private val bookmarksDao: BookmarksDao,
    private val syncManager: SyncWorks,
) {
    suspend operator fun invoke(
        bookmark: Bookmark
    ) {
        // The id has to be settled once and then used for both the row and the sync job.
        //
        // This used to generate a timestamp here while Bookmark's secondary constructor had
        // already generated its own, then store the row under this one and schedule the sync with
        // the other. The two agree only while both calls land in the same second. When they
        // straddle a tick the worker looks up an id that was never stored, throws
        // BookmarkNotFoundException and retries for as long as WorkManager lets it, and the
        // bookmark never reaches the server.
        //
        // It also fixes the unique work name. That is built from the id, so with a stale id every
        // create queued as sync_bookmark_CREATE_<same value> and ExistingWorkPolicy.REPLACE
        // cancelled the previous bookmark's pending sync.
        val bookmarkWithTempId = bookmark.copy(id = reserveTemporaryId())
        bookmarksDao.insertBookmark(bookmarkWithTempId.toEntityModel())
        syncManager.scheduleSyncWork(SyncOperationType.CREATE, bookmarkWithTempId)
    }

    /**
     * Epoch seconds, stepped forward past anything already stored.
     *
     * Two bookmarks added inside the same second produced the same id, and the row insert replaces
     * on conflict, so the first one was quietly overwritten. Stepping forward keeps the value above
     * the 1_000_000 threshold that marks an id as temporary.
     */
    private suspend fun reserveTemporaryId(): Int {
        var candidate = (System.currentTimeMillis() / 1000).toInt()
        while (bookmarksDao.getBookmarkById(candidate) != null) {
            candidate++
        }
        return candidate
    }
}
