package com.desarrollodroide.data.repository

import androidx.work.*
import com.desarrollodroide.data.repository.workers.SyncWorker
import java.util.concurrent.TimeUnit

class SyncManagerImpl(
    private val workManager: WorkManager
) : SyncManager {
    override fun scheduleSyncWork(operationType: SyncOperationType, bookmarkId: Int) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(
                "operationType" to operationType.name,
                "bookmarkId" to bookmarkId
            ))
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setConstraints(constraints)
            .build()

        workManager.beginUniqueWork(
            "sync_bookmark_${operationType.name}_$bookmarkId",
            ExistingWorkPolicy.REPLACE,
            listOf(syncWorkRequest)
        ).enqueue()
    }
}
enum class SyncOperationType {
    CREATE, UPDATE, DELETE
}