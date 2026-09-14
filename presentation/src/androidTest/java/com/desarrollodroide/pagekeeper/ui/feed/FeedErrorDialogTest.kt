package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Every failure in the feed gets its dialog, not only the first one. */
@RunWith(AndroidJUnit4::class)
class FeedErrorDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private var error by mutableStateOf<String?>(null)

    private fun host(onSessionExpired: () -> Unit = {}) {
        rule.setContent {
            ShioriTheme {
                FeedErrorDialog(error = error, onAccept = { error = null }, onSessionExpired = onSessionExpired)
            }
        }
    }

    @Test
    fun aSecondFailureAfterAcceptingTheFirstIsShown() {
        host()

        rule.runOnIdle { error = "Could not add tags" }
        rule.onNodeWithText("Accept").performClick()
        rule.runOnIdle { error = "Could not add tags" }

        rule.onNodeWithText("Could not add tags").assertExists()
    }

    /** The pair (R7): accepting does close the dialog. */
    @Test
    fun acceptingClosesIt() {
        host()

        rule.runOnIdle { error = "Could not add tags" }
        rule.onNodeWithText("Accept").performClick()

        rule.onNodeWithText("Could not add tags").assertDoesNotExist()
    }
}
