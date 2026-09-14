package com.desarrollodroide.pagekeeper.ui.feed

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Back in selection mode clears the selection instead of closing the app. Seen on a device. */
@RunWith(AndroidJUnit4::class)
class SelectionBackHandlerTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backWithASelectionClearsIt() {
        var cleared = 0
        rule.setContent { SelectionBackHandler(selectionActive = true, onClearSelection = { cleared++ }) }

        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()

        assertEquals(1, cleared)
        assertFalse("the activity must stay open", rule.activity.isFinishing)
    }

    /** The pair (R7): with nothing selected Back is not taken. */
    @Test
    fun backWithoutASelectionIsLeftToTheScreen() {
        var cleared = 0
        rule.setContent { SelectionBackHandler(selectionActive = false, onClearSelection = { cleared++ }) }
        rule.waitForIdle()

        assertEquals(0, cleared)
        assertTrue("no handler of ours may be enabled", !rule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
    }
}
