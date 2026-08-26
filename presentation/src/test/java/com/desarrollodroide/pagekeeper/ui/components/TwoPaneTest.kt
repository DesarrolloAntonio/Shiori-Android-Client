package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * When the feed and an article are allowed to sit side by side.
 *
 * Both halves matter. The preference on its own must not split a phone into two useless columns,
 * and a wide window on its own must not override someone who asked for one pane.
 */
class TwoPaneTest {

    @Test
    fun `a wide window with the preference on gets two panes`() {
        assertTrue(shouldUseTwoPanes(preferenceEnabled = true, availableWidth = 1280.dp))
    }

    @Test
    fun `a wide window with the preference off stays on one pane`() {
        assertFalse(shouldUseTwoPanes(preferenceEnabled = false, availableWidth = 1280.dp))
    }

    /** A tablet held upright is 800dp, just under the breakpoint, and stays a single grid. */
    @Test
    fun `a window narrower than the breakpoint stays on one pane even with the preference on`() {
        assertFalse(shouldUseTwoPanes(preferenceEnabled = true, availableWidth = 800.dp))
    }

    @Test
    fun `a phone stays on one pane`() {
        assertFalse(shouldUseTwoPanes(preferenceEnabled = true, availableWidth = 411.dp))
    }

    @Test
    fun `the breakpoint itself counts as wide enough`() {
        assertTrue(shouldUseTwoPanes(preferenceEnabled = true, availableWidth = TwoPaneMinWidth))
    }
}
