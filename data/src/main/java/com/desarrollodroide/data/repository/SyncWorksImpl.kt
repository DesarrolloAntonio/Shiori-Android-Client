package com.desarrollodroide.data.repository

import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.desarrollodroide.data.extensions.toJson
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.repository.workers.SyncWorker
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.model.UpdateCachePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SyncWorksImpl(
    private val workManager: WorkManager,
    private val bookmarksDao: BookmarksDao,
) : SyncWorks {
    override fun scheduleSyncWork(
        operationType: SyncOperationType,
        bookmark: Bookmark,
        updateCachePayload: UpdateCachePayload?
    ) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val encodedTitle = URLEncoder.encode(bookmark.title, "UTF-8")
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(
                "operationType" to operationType.name,
                "bookmarkId" to bookmark.id,
                "updateCachePayload" to updateCachePayload?.toJson()
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

    override fun getPendingJobs(): Flow<List<PendingJob>> =
        workManager.getWorkInfosByTagLiveData("worker_${SyncWorker::class.java.name}")
            .asFlow()
            .map { workInfos ->
                workInfos
                    .filter { !it.state.isFinished }
                    .mapNotNull { workInfo ->
                        Log.d("SyncManager", "WorkInfo: id=${workInfo.id}, state=${workInfo.state}, tags=${workInfo.tags}")

                        val operationType = workInfo.getSyncOperationType()
                        Log.d("SyncManager", "OperationType: $operationType")

                        operationType?.let {
                            PendingJob(
                                operationType = it,
                                state = workInfo.state.name,
                                bookmarkId = workInfo.getBookmarkId() ?: -1,
                                bookmarkTitle = workInfo.getBookmarkTitle() ?: "Unknown",
                            )
                        }
                    }
                    .also { jobs ->
                        Log.d("SyncManager", "Pending Jobs: ${jobs.size}")
                    }
            }
            .flowOn(Dispatchers.IO)


    override fun cancelAllSyncWorkers() {
        workManager.cancelAllWorkByTag(SyncWorker::class.java.name)
    }

    override suspend fun retryAllPendingJobs() {
        val allWorkInfos = withContext(Dispatchers.IO) {
            workManager.getWorkInfosByTag("worker_${SyncWorker::class.java.name}").get()
        }.filter { !it.state.isFinished }

        allWorkInfos.forEach { workInfo ->
            val operationType = workInfo.getSyncOperationType()
            val bookmarkId = workInfo.getBookmarkId()

            if (operationType != null && bookmarkId != null) {
                val bookmark = bookmarksDao.getBookmarkById(bookmarkId)?.toDomainModel()

                if (bookmark != null) {
                    scheduleSyncWork(operationType, bookmark)
                }
            }
        }
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
