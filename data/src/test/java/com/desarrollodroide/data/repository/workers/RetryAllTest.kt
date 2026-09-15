package com.desarrollodroide.data.repository.workers

import androidx.work.WorkInfo
import com.desarrollodroide.data.repository.isShownAsPending
import com.desarrollodroide.data.repository.shouldRequeue
import com.desarrollodroide.model.SyncOperationType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The sync sheet and its "Retry all".
 *
 * Seen on a device, offline: "Update cache" queued a CACHE job, "Retry all" re-created it without
 * its payload (which only the original request carries), it failed five times on reconnect, ended
 * FAILED and vanished from the sheet and the badge. The update never happened and nothing said so.
 */
class RetryAllTest {

    @Test
    fun `retry all never re-creates a cache job, whose payload it cannot carry`() {
        assertFalse(shouldRequeue(SyncOperationType.CACHE, WorkInfo.State.ENQUEUED, rowExists = true))
    }

    @Test
    fun `retry all never replaces a job that is running`() {
        assertFalse(shouldRequeue(SyncOperationType.CREATE, WorkInfo.State.RUNNING, rowExists = true))
    }

    @Test
    fun `a job that failed for good stays in the sheet`() {
        assertTrue(WorkInfo.State.FAILED.isShownAsPending())
    }

    @Test
    fun `retry all gives a failed create another go`() {
        assertTrue(shouldRequeue(SyncOperationType.CREATE, WorkInfo.State.FAILED, rowExists = true))
    }

    /** The pairs (R7): a waiting edit is still retried; a finished success is not listed. */
    @Test
    fun `a waiting update is still retried and a success is not listed`() {
        assertTrue(shouldRequeue(SyncOperationType.UPDATE, WorkInfo.State.ENQUEUED, rowExists = true))
        assertFalse(WorkInfo.State.SUCCEEDED.isShownAsPending())
    }
}
