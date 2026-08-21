package com.desarrollodroide.pagekeeper.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.TagListSaver
import com.desarrollodroide.pagekeeper.ui.components.TagSaver
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Both activities used to be locked to portrait, so the activity was never recreated underneath a
 * screen and plain remember was good enough. Unlocking rotation for tablets and foldables made
 * every one of those a place where the user's work disappears, so the state that matters now goes
 * through savers. These pin the savers themselves; Tag is a plain data class and cannot go into a
 * Bundle without them.
 */
@RunWith(AndroidJUnit4::class)
class StateRestorationTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun assignedTagsSurviveRecreation() {
        val tester = StateRestorationTester(rule)
        tester.setContent {
            ShioriTheme {
                var tags by rememberSaveable(stateSaver = TagListSaver) {
                    mutableStateOf(listOf(Tag(id = 7, name = "kotlin")))
                }
                Column {
                    Text(text = tags.joinToString { it.name })
                    TextButton(onClick = { tags = tags + Tag(id = 9, name = "android") }) {
                        Text(text = "assign")
                    }
                }
            }
        }

        rule.onNodeWithText("assign").performClick()
        rule.onNodeWithText("kotlin, android").assertIsDisplayed()

        tester.emulateSavedInstanceStateRestore()

        rule.onNodeWithText("kotlin, android").assertIsDisplayed()
    }

    @Test
    fun anOpenTagDialogSurvivesRecreation() {
        val tester = StateRestorationTester(rule)
        tester.setContent {
            ShioriTheme {
                var selected by rememberSaveable(stateSaver = TagSaver) {
                    mutableStateOf<Tag?>(null)
                }
                Column {
                    Text(text = selected?.name ?: "nothing selected")
                    TextButton(onClick = { selected = Tag(id = 3, name = "reading") }) {
                        Text(text = "select")
                    }
                }
            }
        }

        rule.onNodeWithText("nothing selected").assertIsDisplayed()
        rule.onNodeWithText("select").performClick()
        rule.onNodeWithText("reading").assertIsDisplayed()

        tester.emulateSavedInstanceStateRestore()

        rule.onNodeWithText("reading").assertIsDisplayed()
    }

    @Test
    fun anEmptyTagSelectionRestoresAsEmptyRatherThanFallingBackToItsInitialValue() {
        val tester = StateRestorationTester(rule)
        tester.setContent {
            ShioriTheme {
                var tags by rememberSaveable(stateSaver = TagListSaver) {
                    mutableStateOf(listOf(Tag(id = 7, name = "kotlin")))
                }
                Column {
                    Text(text = tags.joinToString { it.name }.ifEmpty { "no tags" })
                    TextButton(onClick = { tags = emptyList() }) { Text(text = "clear") }
                }
            }
        }

        rule.onNodeWithText("clear").performClick()
        rule.onNodeWithText("no tags").assertIsDisplayed()

        tester.emulateSavedInstanceStateRestore()

        // A saver that emits an empty list hands back "couldn't restore" and the initial value
        // comes back instead, silently re-adding a tag the user had just removed.
        rule.onNodeWithText("no tags").assertIsDisplayed()
    }

    @Test
    fun aClearedSingleTagRestoresAsClearedRatherThanFallingBackToItsInitialValue() {
        val tester = StateRestorationTester(rule)
        tester.setContent {
            ShioriTheme {
                var selected by rememberSaveable(stateSaver = TagSaver) {
                    mutableStateOf<Tag?>(Tag(id = 3, name = "reading"))
                }
                Column {
                    Text(text = selected?.name ?: "nothing selected")
                    TextButton(onClick = { selected = null }) { Text(text = "clear") }
                }
            }
        }

        rule.onNodeWithText("clear").performClick()
        rule.onNodeWithText("nothing selected").assertIsDisplayed()

        tester.emulateSavedInstanceStateRestore()

        // Same trap as the list: "the user chose nothing" must not decay into "nothing was saved".
        rule.onNodeWithText("nothing selected").assertIsDisplayed()
    }
}
