package com.desarrollodroide.pagekeeper.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.pagekeeper.ui.feed.TopBar
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The app bar's search field.
 *
 * It used to be a Surface styled to look like a field whose only job was to open a full screen
 * search sheet, so it invited typing and answered with a different screen carrying a second,
 * identical looking box. These tests pin the field down as a real one: text goes in where it is
 * tapped, and the feed behind it is what filters.
 */
@RunWith(AndroidJUnit4::class)
class SearchFieldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun typingInTheAppBarReportsTheQuery() {
        var query by mutableStateOf("")
        rule.setContent {
            ShioriTheme {
                TopBar(
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onClearSearch = { query = "" },
                    onFilterClick = { },
                    onSettingsClick = { },
                    onSyncClick = { },
                    selectedTagsCount = 0,
                    showOnlyHiddenTag = false,
                    pendingJobsCount = 0,
                    pendingJobs = emptyList(),
                )
            }
        }

        rule.onNodeWithContentDescription("Search bookmarks").performTextInput("kotlin")

        assertEquals("the field must edit in place, not hand off to another screen", "kotlin", query)
        rule.onNodeWithContentDescription("Search bookmarks").assertTextEquals("kotlin")
    }

    /**
     * The placeholder is what tells the user the bar is for searching, so it has to be visible
     * while the field is empty and gone once it is not.
     */
    @Test
    fun placeholderShowsOnlyWhileEmpty() {
        var query by mutableStateOf("")
        rule.setContent {
            ShioriTheme {
                TopBar(
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onClearSearch = { query = "" },
                    onFilterClick = { },
                    onSettingsClick = { },
                    onSyncClick = { },
                    selectedTagsCount = 0,
                    showOnlyHiddenTag = false,
                    pendingJobsCount = 0,
                    pendingJobs = emptyList(),
                )
            }
        }

        rule.onNodeWithText("Search bookmarks").assertIsDisplayed()

        rule.onNodeWithContentDescription("Search bookmarks").performTextInput("kotlin")
        rule.waitForIdle()

        rule.onNodeWithText("Search bookmarks").assertDoesNotExist()
    }

    /**
     * Clear only appears when there is something to clear. It shares the row with the field, so an
     * always-present button would eat width from an empty box for no reason.
     */
    @Test
    fun clearAppearsWithTextAndEmptiesTheField() {
        var query by mutableStateOf("")
        var cleared = false
        rule.setContent {
            ShioriTheme {
                TopBar(
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onClearSearch = { cleared = true; query = "" },
                    onFilterClick = { },
                    onSettingsClick = { },
                    onSyncClick = { },
                    selectedTagsCount = 0,
                    showOnlyHiddenTag = false,
                    pendingJobsCount = 0,
                    pendingJobs = emptyList(),
                )
            }
        }

        rule.onNodeWithContentDescription("Clear search").assertDoesNotExist()

        rule.onNodeWithContentDescription("Search bookmarks").performTextInput("kotlin")
        rule.waitForIdle()

        rule.onNodeWithContentDescription("Clear search").performClick()
        rule.waitForIdle()

        assertTrue("clear must report back so the feed drops the filter", cleared)
        assertEquals("", query)
        rule.onNodeWithText("Search bookmarks").assertIsDisplayed()
    }
}
