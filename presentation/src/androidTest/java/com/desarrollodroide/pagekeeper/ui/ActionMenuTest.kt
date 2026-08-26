package com.desarrollodroide.pagekeeper.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.UpdateCacheDialog
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.ButtonsView
import com.desarrollodroide.pagekeeper.ui.tags.TagsList
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The action menus: the row on each bookmark card, the per-tag overflow menu, and the update cache
 * dialog.
 *
 * Two things are worth pinning here and neither is visible by looking at the screen. Which actions
 * are offered is conditional — an epub action only for bookmarks that have one, an update action
 * only once the server has really created the bookmark — and every icon looks the same whether it
 * is wired to its own callback or to its neighbour's.
 */
@RunWith(AndroidJUnit4::class)
class ActionMenuTest {

    @get:Rule
    val rule = createComposeRule()

    /** A bookmark the server has accepted. Temporary ids are timestamps, so they are large. */
    private fun saved(hasEbook: Boolean = false) =
        Bookmark.mock().copy(id = 42, hasEbook = hasEbook)

    /** Still queued: AddBookmarkUseCase stamps System.currentTimeMillis()/1000 as a placeholder. */
    private fun pending() = Bookmark.mock().copy(id = 1_724_000_000, hasEbook = false)

    private fun recordingActions(log: MutableList<String>) = BookmarkActions(
        onClickEdit = { log += "edit" },
        onClickDelete = { log += "delete" },
        onClickShare = { log += "share" },
        onClickCategory = { log += "category" },
        onClickBookmark = { log += "bookmark" },
        onClickEpub = { log += "epub" },
        onClickSync = { log += "sync" },
    )

    private fun setCardActions(bookmark: Bookmark, log: MutableList<String>) {
        rule.setContent {
            ShioriTheme {
                ButtonsView(
                    getBookmark = { bookmark },
                    actions = recordingActions(log),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    @Test
    fun everyBookmarkOffersEditShareAndDelete() {
        setCardActions(saved(), mutableListOf())

        rule.onNodeWithContentDescription("Edit").assertIsDisplayed()
        rule.onNodeWithContentDescription("Share").assertIsDisplayed()
        rule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }

    @Test
    fun theEpubActionAppearsOnlyForABookmarkThatHasOne() {
        setCardActions(saved(hasEbook = true), mutableListOf())
        rule.onNodeWithContentDescription("Epub").assertIsDisplayed()
    }

    @Test
    fun theEpubActionIsAbsentForABookmarkWithoutOne() {
        setCardActions(saved(hasEbook = false), mutableListOf())
        rule.onNodeWithContentDescription("Epub").assertDoesNotExist()
    }

    /**
     * Offering "update cache" for a bookmark the server has never seen sends it an id that does not
     * exist there yet.
     */
    @Test
    fun theUpdateActionIsHiddenUntilTheServerHasProcessedTheBookmark() {
        setCardActions(pending(), mutableListOf())
        rule.onNodeWithContentDescription("Update").assertDoesNotExist()
    }

    @Test
    fun theUpdateActionIsOfferedOnceTheBookmarkExistsOnTheServer() {
        setCardActions(saved(), mutableListOf())
        rule.onNodeWithContentDescription("Update").assertIsDisplayed()
    }

    @Test
    fun eachCardActionInvokesOnlyItsOwnCallback() {
        val log = mutableListOf<String>()
        setCardActions(saved(hasEbook = true), log)

        rule.onNodeWithContentDescription("Edit").performClick()
        rule.onNodeWithContentDescription("Share").performClick()
        rule.onNodeWithContentDescription("Epub").performClick()
        rule.onNodeWithContentDescription("Update").performClick()
        rule.onNodeWithContentDescription("Delete").performClick()

        assertEquals(listOf("edit", "share", "epub", "sync", "delete"), log)
    }

    private fun setTagsList(renamed: MutableList<String>, deleted: MutableList<String>) {
        rule.setContent {
            ShioriTheme {
                TagsList(
                    tags = listOf(Tag(id = 1, name = "android", selected = false, nBookmarks = 3)),
                    onRename = { renamed += it.name },
                    onDelete = { deleted += it.name },
                )
            }
        }
    }

    @Test
    fun theTagOverflowMenuOffersRenameAndDelete() {
        setTagsList(mutableListOf(), mutableListOf())

        rule.onNodeWithContentDescription("Actions for android").performClick()

        rule.onNodeWithText("Rename").assertIsDisplayed()
        rule.onNodeWithText("Delete").assertIsDisplayed()
    }

    @Test
    fun theTagMenuRoutesRenameAndDeleteSeparately() {
        val renamed = mutableListOf<String>()
        val deleted = mutableListOf<String>()
        setTagsList(renamed, deleted)

        rule.onNodeWithContentDescription("Actions for android").performClick()
        rule.onNodeWithText("Rename").performClick()

        assertEquals(listOf("android"), renamed)
        assertTrue("delete must not fire when rename was chosen", deleted.isEmpty())

        rule.onNodeWithContentDescription("Actions for android").performClick()
        rule.onNodeWithText("Delete").performClick()

        assertEquals(listOf("android"), deleted)
        assertEquals("rename must not fire twice", 1, renamed.size)
    }

    /**
     * These three checkboxes are the payload of the update-cache call. They already went out wrong
     * once: the request was serialised with camelCase names the Go server does not read, so it
     * dropped them silently and the boxes did nothing.
     */
    @Test
    fun theUpdateCacheDialogReportsExactlyTheBoxesTheUserChecked() {
        var reported: Triple<Boolean, Boolean, Boolean>? = null
        rule.setContent {
            ShioriTheme {
                UpdateCacheDialog(
                    isLoading = false,
                    showDialog = mutableStateOf(true),
                    onConfirm = { keepOldTitle, updateArchive, updateEbook ->
                        reported = Triple(keepOldTitle, updateArchive, updateEbook)
                    },
                )
            }
        }

        rule.onNodeWithText("Keep the old title and excerpt").performClick()
        rule.onNodeWithText("Update Ebook as well").performClick()
        rule.onNodeWithText("Update").performClick()

        assertEquals(Triple(true, false, true), reported)
    }
}
