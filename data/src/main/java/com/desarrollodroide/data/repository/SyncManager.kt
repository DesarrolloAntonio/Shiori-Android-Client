package com.desarrollodroide.data.repository

import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import kotlinx.coroutines.flow.Flow

interface SyncManager {

    fun scheduleSyncWork(operationType: SyncOperationType, bookmarkId: Int)
    fun getPendingJobs(): Flow<List<PendingJob>>
    fun cancelAllSyncWorkers()

}