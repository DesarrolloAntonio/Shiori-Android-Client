package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * When the bookmark editor runs in two columns.
 *
 * In one column on a landscape tablet it used 460dp of a 960dp window and still did not fit: Tags
 * was cut in half by the save bar and the suggestions were off screen, with two thirds of the
 * width blank beside them.
 */
class FormLayoutTest {

    @Test
    fun `a landscape tablet splits the form`() {
        // Pixel Tablet, landscape: 960x600dp.
        assertTrue(shouldSplitFormIntoTwoColumns(availableWidth = 960.dp, availableHeight = 600.dp))
        // And a 16:9 tablet, which is shorter still.
        assertTrue(shouldSplitFormIntoTwoColumns(availableWidth = 960.dp, availableHeight = 540.dp))
    }

    @Test
    fun `a phone in portrait keeps one column`() {
        assertFalse(shouldSplitFormIntoTwoColumns(availableWidth = 411.dp, availableHeight = 891.dp))
    }

    @Test
    fun `an unfolded foldable splits, because the form gets far less than the window`() {
        // The window is 883dp tall; measured on the device, the form is handed 615dp of it once
        // the app bar, the save bar and the insets have taken their share. In one column the add
        // form wants about 650dp, so it would not fit.
        assertTrue(shouldSplitFormIntoTwoColumns(availableWidth = 851.dp, availableHeight = 615.dp))
    }

    @Test
    fun `a tall window keeps one column`() {
        assertFalse(shouldSplitFormIntoTwoColumns(availableWidth = 851.dp, availableHeight = 700.dp))
    }

    @Test
    fun `a landscape phone stays in one column because it is too narrow to split`() {
        // Short enough to want it, but two columns here would each be narrower than a phone.
        assertFalse(shouldSplitFormIntoTwoColumns(availableWidth = 640.dp, availableHeight = 360.dp))
    }

    @Test
    fun `a tablet in portrait keeps one column`() {
        assertFalse(shouldSplitFormIntoTwoColumns(availableWidth = 800.dp, availableHeight = 1280.dp))
    }
}
