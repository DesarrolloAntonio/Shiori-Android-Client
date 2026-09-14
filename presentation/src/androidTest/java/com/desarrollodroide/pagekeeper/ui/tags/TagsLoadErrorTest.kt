package com.desarrollodroide.pagekeeper.ui.tags

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A tags list that failed to load says so.
 *
 * With nothing cached and the server refusing, the screen said "No tags yet" while the server had
 * eight: the error was set in the view model and never read. Seen on a device against a fake
 * server answering 401.
 */
@RunWith(AndroidJUnit4::class)
class TagsLoadErrorTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun aFailedLoadWithNothingCachedShowsTheErrorAndRetry() {
        var retries = 0
        rule.setContent {
            ShioriTheme {
                Box {
                    TagsContent(tags = emptyList(), isLoading = false, loadError = "unauthorized", onRetry = { retries++ }, onRename = {}, onDelete = {})
                }
            }
        }

        rule.onNodeWithText("No tags yet").assertDoesNotExist()
        rule.onNodeWithText("Couldn't load tags").assertExists()
        rule.onNodeWithText("Retry").performClick()
        rule.runOnIdle { assertEquals(1, retries) }
    }

    /** The pair (R7): an empty list that loaded fine is still "No tags yet". */
    @Test
    fun anEmptyListThatLoadedIsStillNoTagsYet() {
        rule.setContent {
            ShioriTheme {
                Box {
                    TagsContent(tags = emptyList(), isLoading = false, loadError = null, onRetry = {}, onRename = {}, onDelete = {})
                }
            }
        }

        rule.onNodeWithText("No tags yet").assertExists()
    }
}
