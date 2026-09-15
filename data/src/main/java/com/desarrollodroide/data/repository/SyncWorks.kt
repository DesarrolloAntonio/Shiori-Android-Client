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
        updateCachePayload: UpdateCachePayload? = null
    )
    fun getPendingJobs(): Flow<List<PendingJob>>
    fun cancelAllSyncWorkers()
    suspend fun retryAllPendingJobs()

    /** Bookmarks holding an edit or a delete that has not reached the server yet. */
    suspend fun bookmarkIdsWithPendingChanges(): Set<Int>

}