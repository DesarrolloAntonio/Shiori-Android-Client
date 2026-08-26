package com.desarrollodroide.pagekeeper.ui.components

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Which devices get the login screen pinned to portrait.
 *
 * The input is the smallest width, a property of the device rather than of how it is being held,
 * so the answer cannot flip as the user rotates.
 */
class PortraitLockTest {

    @Test
    fun `a phone is pinned`() {
        assertTrue(shouldLockToPortrait(smallestWidthDp = 411))
    }

    @Test
    fun `a folding phone is pinned while it is folded and free once it opens`() {
        assertTrue(shouldLockToPortrait(smallestWidthDp = 411))
        assertFalse(shouldLockToPortrait(smallestWidthDp = 851))
    }

    @Test
    fun `a tablet is left alone`() {
        assertFalse(shouldLockToPortrait(smallestWidthDp = 600))
        assertFalse(shouldLockToPortrait(smallestWidthDp = 800))
    }
}
