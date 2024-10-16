package com.desarrollodroide.data.repository

import android.util.Log
import androidx.work.*
import com.desarrollodroide.data.repository.workers.SyncWorker
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
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
            .addTag(SyncWorker::class.java.name)
            .addTag(operationType.name)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
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

    override fun getPendingJobs(): Flow<List<PendingJob>> = flow {
        val allWorkInfos = withContext(Dispatchers.IO) {
            workManager.getWorkInfosByTag(SyncWorker::class.java.name).get()
        }

        Log.d("SyncManager", "Total WorkInfos: ${allWorkInfos.size}")

        val pendingJobs = allWorkInfos
            .filter { !it.state.isFinished }
            .mapNotNull { workInfo ->
                Log.d("SyncManager", "WorkInfo: id=${workInfo.id}, state=${workInfo.state}, tags=${workInfo.tags}")

                val operationType = workInfo.getSyncOperationType()
                Log.d("SyncManager", "OperationType: $operationType")

                operationType?.let {
                    PendingJob(
                        operationType = it,
                        state = workInfo.state.name,
                        bookmarkId = workInfo.getBookmarkId()
                    )
                }
            }

        Log.d("SyncManager", "Pending Jobs: ${pendingJobs.size}")
        emit(pendingJobs)
    }

    override fun cancelAllSyncWorkers() {
        workManager.cancelAllWorkByTag(SyncWorker::class.java.name)
    }

    fun WorkInfo.getSyncOperationType(): SyncOperationType? {
        val operationTypeString = tags.firstOrNull { it != SyncWorker::class.java.name }
        return SyncOperationType.fromString(operationTypeString ?: return null).also {
            Log.d("SyncManager", "Parsed SyncOperationType: $it from tag: $operationTypeString")
        }
    }

    fun WorkInfo.getBookmarkId(): Int? {
        val bookmarkIdString = id.toString().split("_").lastOrNull()
        return bookmarkIdString?.toIntOrNull().also {
            Log.d("SyncManager", "BookmarkId: $it")
        }
    }

}
