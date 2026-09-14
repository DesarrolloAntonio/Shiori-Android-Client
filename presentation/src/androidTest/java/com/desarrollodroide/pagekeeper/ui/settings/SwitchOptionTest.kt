package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A settings switch is one control: the row, named by its title, that is on or off.
 *
 * The row was clickable and the Switch inside it took clicks of its own, so a screen reader
 * stopped twice per setting and the second stop, the switch, had no name. Flagged by the harness's
 * accessibility check on a device.
 */
@RunWith(AndroidJUnit4::class)
class SwitchOptionTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun theRowIsTheOnlyToggleAndCarriesItsTitle() {
        rule.setContent {
            ShioriTheme {
                SwitchOption(title = "Compact view", icon = Icons.Filled.ViewCompactAlt, checked = false, onCheckedChange = {})
            }
        }

        rule.onAllNodes(isToggleable()).assertCountEquals(1)
        rule.onAllNodes(isToggleable() and hasText("Compact view")).assertCountEquals(1)
    }

    /** The pair (R7): tapping the row still switches it. */
    @Test
    fun tappingTheRowSwitchesIt() {
        var checked by mutableStateOf(false)
        rule.setContent {
            ShioriTheme {
                SwitchOption(title = "Compact view", icon = Icons.Filled.ViewCompactAlt, checked = checked, onCheckedChange = { checked = it })
            }
        }

        rule.onNodeWithText("Compact view").performClick()

        rule.onAllNodes(isToggleable())[0].assertIsOn()
    }
}
