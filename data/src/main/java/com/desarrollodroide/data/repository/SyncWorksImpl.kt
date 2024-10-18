package com.desarrollodroide.data.repository

import android.util.Log
import androidx.work.*
import com.desarrollodroide.data.repository.workers.SyncWorker
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SyncWorksImpl(
    private val workManager: WorkManager
) : SyncWorks {
    override fun scheduleSyncWork(
        operationType: SyncOperationType,
        bookmark: Bookmark
    ) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val encodedTitle = URLEncoder.encode(bookmark.title, "UTF-8")
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(
                "operationType" to operationType.name,
                "bookmarkId" to bookmark.id
            ))
            .addTag("worker_${SyncWorker::class.java.name}")
            .addTag("operationType_${operationType.name}")
            .addTag("bookmarkId_${bookmark.id}")
            .addTag("bookmarkTitle_$encodedTitle")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setConstraints(constraints)
            .build()

        workManager.beginUniqueWork(
            "sync_bookmark_${operationType.name}_${bookmark.id}",
            ExistingWorkPolicy.REPLACE,
            listOf(syncWorkRequest)
        ).enqueue()
    }

    override fun getPendingJobs(): Flow<List<PendingJob>> = flow {
        val allWorkInfos = withContext(Dispatchers.IO) {
            workManager.getWorkInfosByTag("worker_${SyncWorker::class.java.name}").get()
        }

        Log.d("SyncManager", "Total WorkInfos: ${allWorkInfos.size}")
        allWorkInfos.forEach {
            Log.d("SyncManagerInfo", "WorkInfo: id=${it.id}, state=${it.state}, tags=${it.tags}")
        }

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
                        bookmarkId = workInfo.getBookmarkId()?:-1,
                        bookmarkTitle = workInfo.getBookmarkTitle()?:"Unknown",
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
        return tags
            .firstOrNull { it.startsWith("operationType_") }
            ?.substringAfter("operationType_")
            ?.let { SyncOperationType.fromString(it) }
            .also { Log.d("SyncManager", "Parsed SyncOperationType: $it") }
    }

    fun WorkInfo.getBookmarkId(): Int? {
        return tags
            .firstOrNull { it.startsWith("bookmarkId_") }
            ?.substringAfter("bookmarkId_")
            ?.toIntOrNull()
            .also { Log.d("SyncManager", "BookmarkId: $it") }
    }

    fun WorkInfo.getBookmarkTitle(): String? {
        return tags
            .firstOrNull { it.startsWith("bookmarkTitle_") }
            ?.substringAfter("bookmarkTitle_")
            ?.let { URLDecoder.decode(it, "UTF-8") }
            .also { Log.d("SyncManager", "BookmarkTitle: $it") }
    }
}
