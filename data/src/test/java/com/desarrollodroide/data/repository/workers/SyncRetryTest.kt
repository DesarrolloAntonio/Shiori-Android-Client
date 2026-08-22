package com.desarrollodroide.data.repository.workers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * A permanently failing sync used to retry until the user cleared the app data. WorkManager backs
 * off exponentially and survives reboots, so "retry" with no ceiling means for ever.
 */
class SyncRetryTest {

    @Test
    fun `the first attempt still has retries left`() {
        assertFalse(hasRunOutOfAttempts(runAttemptCount = 0))
    }

    @Test
    fun `an attempt below the limit still retries`() {
        assertFalse(hasRunOutOfAttempts(runAttemptCount = MAX_SYNC_ATTEMPTS - 3))
    }

    /** runAttemptCount is zero based, so the last allowed run is maxAttempts - 1. */
    @Test
    fun `the last allowed attempt gives up rather than scheduling another`() {
        assertTrue(hasRunOutOfAttempts(runAttemptCount = MAX_SYNC_ATTEMPTS - 1))
    }

    @Test
    fun `an attempt past the limit gives up`() {
        assertTrue(hasRunOutOfAttempts(runAttemptCount = MAX_SYNC_ATTEMPTS + 5))
    }
}
