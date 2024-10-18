package com.desarrollodroide.data.repository

import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import kotlinx.coroutines.flow.Flow

interface SyncWorks {

    fun scheduleSyncWork(operationType: SyncOperationType, bookmark: Bookmark)
    fun getPendingJobs(): Flow<List<PendingJob>>
    fun cancelAllSyncWorkers()
    suspend fun retryAllPendingJobs()

}