package com.desarrollodroide.data.repository.workers

import androidx.work.WorkInfo
import com.desarrollodroide.data.repository.holdsLocalChange
import com.desarrollodroide.model.SyncOperationType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Which jobs keep a refresh from writing over their bookmark.
 *
 * Seen on a device (QA campaign, process 04, #13): an edit's upload failed once, a pull to refresh
 * wrote the server's copy over the row while the retry waited, and the retry uploaded that copy.
 */
class PendingChangesTest {

    @Test
    fun `an edit or a delete that has not reached the server holds its bookmark, a failed one too`() {
        assertTrue(holdsLocalChange(SyncOperationType.UPDATE, WorkInfo.State.ENQUEUED))
        assertTrue(holdsLocalChange(SyncOperationType.DELETE, WorkInfo.State.RUNNING))
        // "Retry all" uploads the row as stored, so a failed edit still needs it.
        assertTrue(holdsLocalChange(SyncOperationType.UPDATE, WorkInfo.State.FAILED))
    }

    /** The pair (R7): an upload that landed, and jobs that carry no local edit, let the refresh write. */
    @Test
    fun `a finished upload, a cache job or a create lets the refresh write`() {
        assertFalse(holdsLocalChange(SyncOperationType.UPDATE, WorkInfo.State.SUCCEEDED))
        assertFalse(holdsLocalChange(SyncOperationType.CACHE, WorkInfo.State.ENQUEUED))
        assertFalse(holdsLocalChange(SyncOperationType.CREATE, WorkInfo.State.ENQUEUED))
    }
}
