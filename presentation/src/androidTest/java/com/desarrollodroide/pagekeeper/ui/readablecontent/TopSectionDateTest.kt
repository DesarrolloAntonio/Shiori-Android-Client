package com.desarrollodroide.pagekeeper.ui.readablecontent

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.pagekeeper.extensions.asLocalBookmarkDate
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The reader's header shows a bookmark's time the way the cards do: a date in the device's zone.
 *
 * It showed the server's timestamp as it came, `2026-08-21 14:26:52`, which is UTC, on a device two
 * hours ahead, over a card that said "Aug 21, 2026". Seen on a release build (QA campaign, process
 * 06, R-01); BM-02's fix had changed the cards only.
 */
@RunWith(AndroidJUnit4::class)
class TopSectionDateTest {

    @get:Rule
    val rule = createComposeRule()

    private val serverTime = "2026-08-21 14:26:52"

    @Test
    fun theFullScreenHeaderShowsTheLocalDateNotTheServerTimestamp() {
        rule.setContent { ShioriTheme { TopSection(title = "Coroutines | Kotlin", date = serverTime, onClick = {}) } }

        rule.onNodeWithText(serverTime).assertDoesNotExist()
        rule.onNodeWithText(serverTime.asLocalBookmarkDate()).assertExists()
    }

    /** The two pane detail has a header of its own. */
    @Test
    fun thePaneHeaderShowsTheLocalDateNotTheServerTimestamp() {
        rule.setContent { ShioriTheme { TopSection(title = "Coroutines | Kotlin", date = serverTime, onClick = {}, onClose = {}) } }

        rule.onNodeWithText(serverTime).assertDoesNotExist()
        rule.onNodeWithText(serverTime.asLocalBookmarkDate()).assertExists()
    }
}
