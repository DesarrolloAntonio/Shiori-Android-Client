package com.desarrollodroide.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.desarrollodroide.data.local.room.database.BookmarksDatabase
import com.desarrollodroide.data.repository.workers.SyncWorker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/**
 * Logging out discards every sync job, including one that already gave up.
 *
 * Seen on a device (QA campaign, process 05): account A's edit failed for good, A logged out —
 * the dialog said the change would be lost — and B signed in. The failed job was still there: B's
 * sync sheet listed A's change, and because a failed edit keeps a refresh from writing its bookmark,
 * B's app never showed that bookmark although the server had it.
 */
class LogoutDiscardsSyncJobsTest {

    @get:Rule
    val timeout: Timeout = Timeout.seconds(30)

    private lateinit var database: BookmarksDatabase
    private lateinit var workManager: WorkManager
    private lateinit var syncWorks: SyncWorksImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(SynchronousExecutor())
                .setTaskExecutor(SynchronousExecutor())
                .build(),
        )
        workManager = WorkManager.getInstance(context)
        database = Room.inMemoryDatabaseBuilder(context, BookmarksDatabase::class.java).allowMainThreadQueries().build()
        syncWorks = SyncWorksImpl(workManager, database.bookmarksDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun anEditThatFailedForGoodIsGoneAfterLogout() = runBlocking {
        val failed = failedEditOf(bookmarkId = 102)
        // The pair (R7): before logout the failed edit is listed and holds its bookmark.
        assertEquals(WorkInfo.State.FAILED, workManager.getWorkInfoById(failed).get()?.state)
        assertEquals(setOf(102), syncWorks.bookmarkIdsWithPendingChanges())

        syncWorks.cancelAllSyncWorkers()

        assertEquals(emptySet<Int>(), syncWorks.bookmarkIdsWithPendingChanges())
        assertEquals(emptyList<WorkInfo>(), workManager.getWorkInfosByTag("worker_${SyncWorker::class.java.name}").get())
    }

    /** A job tagged exactly as `SyncWorksImpl.scheduleSyncWork` tags one, whose worker gives up at once. */
    private fun failedEditOf(bookmarkId: Int) = OneTimeWorkRequestBuilder<GivesUp>()
        .addTag(SyncWorker::class.java.name)
        .addTag("worker_${SyncWorker::class.java.name}")
        .addTag("operationType_UPDATE")
        .addTag("bookmarkId_$bookmarkId")
        .addTag("bookmarkTitle_QA_Acc01")
        .build()
        .also { workManager.enqueue(it).result.get() }
        .id

    class GivesUp(context: Context, params: WorkerParameters) : Worker(context, params) {
        override fun doWork(): Result = Result.failure()
    }
}
