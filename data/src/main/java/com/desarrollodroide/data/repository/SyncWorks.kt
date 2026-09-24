package com.desarrollodroide.data.repository

import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.model.UpdateCachePayload
import kotlinx.coroutines.flow.Flow

interface SyncWorks {

    fun scheduleSyncWork(
        operationType: SyncOperationType,
        bookmark: Bookmark,
        updateCachePayload: UpdateCachePayload? = null,
        removedTagNames: Set<String> = emptySet(),
    )
    fun getPendingJobs(): Flow<List<PendingJob>>
    fun cancelAllSyncWorkers()
    suspend fun retryAllPendingJobs()

    /**
     * Tags the user took off [bookmarkId] in an edit that has not reached the server yet. A new
     * edit replaces the waiting job, so it has to carry these on as well.
     */
    suspend fun pendingTagRemovals(bookmarkId: Int): Set<String>

    /** Bookmarks holding an edit or a delete that has not reached the server yet. */
    suspend fun bookmarkIdsWithPendingChanges(): Set<Int>

}