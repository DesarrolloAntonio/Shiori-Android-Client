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
                    .filter { it.state.isShownAsPending() }
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
        // Cancelling only stops what has not finished. A job that already gave up stayed in the
        // database, so the next account's sync sheet listed it, and since a failed edit keeps a
        // refresh from writing its bookmark, that bookmark never appeared for the next account.
        // Logout says pending changes are lost; pruning makes it so. SyncWorker is the app's only
        // worker, so nothing else is removed.
        workManager.pruneWork()
    }

    override suspend fun retryAllPendingJobs() {
        val allWorkInfos = withContext(Dispatchers.IO) {
            workManager.getWorkInfosByTag("worker_${SyncWorker::class.java.name}").get()
        }

        allWorkInfos.forEach { workInfo ->
            val operationType = workInfo.getSyncOperationType()
            val bookmarkId = workInfo.getBookmarkId()

            if (operationType != null && bookmarkId != null) {
                val bookmark = bookmarksDao.getBookmarkById(bookmarkId)?.toDomainModel()

                if (shouldRequeue(operationType, workInfo.state, rowExists = bookmark != null) && bookmark != null) {
                    scheduleSyncWork(operationType, bookmark)
                }
            }
        }
    }

    override suspend fun bookmarkIdsWithPendingChanges(): Set<Int> = withContext(Dispatchers.IO) {
        workManager.getWorkInfosByTag("worker_${SyncWorker::class.java.name}").get()
            .filter { holdsLocalChange(it.getSyncOperationType(), it.state) }
            .mapNotNull { it.getBookmarkId() }
            .toSet()
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

/**
 * Whether the sync sheet (and its badge) lists a job: everything not done yet, and a job that gave
 * up. A failed job used to drop out of the list the moment it failed, so a change that never
 * reached the server left no trace anywhere.
 */
internal fun WorkInfo.State.isShownAsPending(): Boolean = !isFinished || this == WorkInfo.State.FAILED

/**
 * Whether a job holds a change that exists only on this device, so a sync must not write the
 * server's copy over its bookmark.
 *
 * An UPDATE uploads the row as it is stored when it runs. A refresh while the upload waited for a
 * retry wrote the server's copy over the row, and the retry then uploaded that copy: the edit was
 * gone and the job reported success. A DELETE has already removed the row, and a refresh put the
 * card back. A failed job counts too, for as long as the sheet lists it: "Retry all" uploads the
 * row as stored.
 */
internal fun holdsLocalChange(operationType: SyncOperationType?, state: WorkInfo.State): Boolean =
    (operationType == SyncOperationType.UPDATE || operationType == SyncOperationType.DELETE) &&
        state.isShownAsPending()

/**
 * Whether "Retry all" re-creates a job.
 *
 * Never a CACHE job: its options travel only in the original request, and the copy made here had
 * none, so it failed on every attempt. Never a running job, which REPLACE would cancel mid-request.
 */
internal fun shouldRequeue(operationType: SyncOperationType, state: WorkInfo.State, rowExists: Boolean): Boolean =
    operationType != SyncOperationType.CACHE &&
        rowExists &&
        state in setOf(WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED, WorkInfo.State.FAILED)
