package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The "Hide tag" sheet opens when the user asks for it, and only then.
 *
 * It used to open whenever the section was composed with tags already loaded, which the view model
 * keeps for good once fetched: after a rotation, or on coming back from Manage tags or Network
 * logs, the sheet the user had closed was back on screen. Seen on a device.
 */
@RunWith(AndroidJUnit4::class)
class HideTagSheetTest {

    @get:Rule
    val rule = createComposeRule()

    private val loaded = UiState(data = listOf(Tag(1, "kotlin"), Tag(2, "android")), idle = false)

    @Test
    fun tagsAlreadyLoadedDoNotOpenTheSheetOnTheirOwn() {
        StateRestorationTester(rule).apply {
            setContent { ShioriTheme { section(loaded) } }
            emulateSavedInstanceStateRestore()
        }
        rule.waitForIdle()

        rule.onNodeWithText("Select category to hide").assertDoesNotExist()
    }

    /** The pair (R7): tapping the row does open it. */
    @Test
    fun tappingHideTagOpensTheSheet() {
        rule.setContent { ShioriTheme { section(loaded) } }

        rule.onNodeWithText("Hide tag").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Select category to hide").assertExists()
    }

    @androidx.compose.runtime.Composable
    private fun section(tags: UiState<List<Tag>>) {
        FeedSection(
            compactView = false,
            onCompactViewChanged = {},
            useTwoPaneLayout = false,
            onUseTwoPaneLayoutChanged = {},
            onClickHideDialogOption = {},
            onHideTagChanged = {},
            tagsUiState = tags,
            hideTag = null,
            onNavigateToTags = {},
        )
    }
}
