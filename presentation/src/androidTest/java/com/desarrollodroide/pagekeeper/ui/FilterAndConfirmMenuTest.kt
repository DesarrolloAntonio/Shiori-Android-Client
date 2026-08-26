package com.desarrollodroide.pagekeeper.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.feed.CategoriesView
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The filter sheet and the confirmation dialog that guards destructive actions.
 */
@RunWith(AndroidJUnit4::class)
class FilterAndConfirmMenuTest {

    @get:Rule
    val rule = createComposeRule()

    private val android = Tag(id = 1, name = "android", selected = false, nBookmarks = 3)
    private val compose = Tag(id = 2, name = "compose", selected = false, nBookmarks = 2)

    /**
     * Cancelling a delete must not delete. Worth its own test because both buttons sit in the same
     * dialog and dismissing still has to close it.
     */
    @Test
    fun cancellingAConfirmationDoesNotRunTheAction() {
        var confirmed = false
        val open = mutableStateOf(true)
        rule.setContent {
            ShioriTheme {
                ConfirmDialog(
                    title = "Confirmation",
                    content = "Are you sure you want to delete this bookmark?",
                    confirmButton = "Delete",
                    dismissButton = "Cancel",
                    onConfirm = { confirmed = true },
                    openDialog = open,
                )
            }
        }

        rule.onNodeWithText("Cancel").performClick()

        assertFalse("cancel must not confirm the delete", confirmed)
        rule.runOnIdle { assertFalse("cancel must close the dialog", open.value) }
    }

    @Test
    fun confirmingADeleteRunsTheAction() {
        var confirmed = false
        val open = mutableStateOf(true)
        rule.setContent {
            ShioriTheme {
                ConfirmDialog(
                    title = "Confirmation",
                    content = "Are you sure you want to delete this bookmark?",
                    confirmButton = "Delete",
                    dismissButton = "Cancel",
                    onConfirm = { confirmed = true },
                    openDialog = open,
                )
            }
        }

        rule.onNodeWithText("Delete").performClick()

        assertTrue("confirm must run the delete", confirmed)
    }

    private fun setFilter(
        tagToHide: Tag? = null,
        selectedOptionIndex: Int = 0,
        selectedTags: List<Tag> = emptyList(),
        selected: MutableList<String> = mutableListOf(),
        deselected: MutableList<String> = mutableListOf(),
        resets: MutableList<Unit> = mutableListOf(),
        dismissals: MutableList<Unit> = mutableListOf(),
        hiddenFilter: MutableList<Boolean> = mutableListOf(),
        optionIndexes: MutableList<Int> = mutableListOf(),
    ) {
        rule.setContent {
            ShioriTheme {
                CategoriesView(
                    onDismiss = { dismissals += Unit },
                    uniqueCategories = listOf(android, compose),
                    tagToHide = tagToHide,
                    onFilterHiddenTag = { hiddenFilter += it },
                    selectedOptionIndex = selectedOptionIndex,
                    onSelectedOptionIndexChanged = { optionIndexes += it },
                    selectedTags = selectedTags,
                    onCategorySelected = { selected += it.name },
                    onCategoryDeselected = { deselected += it.name },
                    onResetAll = { resets += Unit },
                )
            }
        }
    }

    @Test
    fun theFilterListsEveryCategoryAndItsControls() {
        setFilter()

        rule.onNodeWithText("Categories").assertIsDisplayed()
        rule.onNodeWithText("android").assertIsDisplayed()
        rule.onNodeWithText("compose").assertIsDisplayed()
        rule.onNodeWithText("Reset All").assertIsDisplayed()
        rule.onNodeWithText("Close").assertIsDisplayed()
    }

    @Test
    fun tappingAnUnselectedCategorySelectsIt() {
        val selected = mutableListOf<String>()
        val deselected = mutableListOf<String>()
        setFilter(selected = selected, deselected = deselected)

        rule.onNodeWithText("android").performClick()

        assertEquals(listOf("android"), selected)
        assertTrue("an unselected tag must not deselect", deselected.isEmpty())
    }

    /** The same chip has to work the other way once the tag is already part of the filter. */
    @Test
    fun tappingAnAlreadySelectedCategoryDeselectsIt() {
        val selected = mutableListOf<String>()
        val deselected = mutableListOf<String>()
        setFilter(selectedTags = listOf(android), selected = selected, deselected = deselected)

        rule.onNodeWithText("android").performClick()

        assertEquals(listOf("android"), deselected)
        assertTrue("an already selected tag must not select again", selected.isEmpty())
    }

    @Test
    fun resetAllClearsTheFilterAndTheHiddenTagToggle() {
        val resets = mutableListOf<Unit>()
        val hiddenFilter = mutableListOf<Boolean>()
        setFilter(selectedTags = listOf(android), resets = resets, hiddenFilter = hiddenFilter)

        rule.onNodeWithText("Reset All").performClick()

        assertEquals(1, resets.size)
        assertEquals("reset must also drop the hidden tag filter", listOf(false), hiddenFilter)
    }

    @Test
    fun closeDismissesWithoutTouchingTheFilter() {
        val dismissals = mutableListOf<Unit>()
        val resets = mutableListOf<Unit>()
        setFilter(dismissals = dismissals, resets = resets)

        rule.onNodeWithText("Close").performClick()

        assertEquals(1, dismissals.size)
        assertTrue("close must not reset anything", resets.isEmpty())
    }

    /** The hidden tag controls only exist when a tag is actually configured as hidden. */
    @Test
    fun theHiddenTagSwitchOnlyAppearsWhenATagIsHidden() {
        val hiddenFilter = mutableListOf<Boolean>()
        val optionIndexes = mutableListOf<Int>()
        setFilter(tagToHide = compose, hiddenFilter = hiddenFilter, optionIndexes = optionIndexes)

        rule.onNodeWithText("Hidden: compose").assertIsDisplayed()
        rule.onNodeWithText("Hidden tag").performClick()

        assertEquals(listOf(1), optionIndexes)
        assertEquals(listOf(true), hiddenFilter)
    }
}
