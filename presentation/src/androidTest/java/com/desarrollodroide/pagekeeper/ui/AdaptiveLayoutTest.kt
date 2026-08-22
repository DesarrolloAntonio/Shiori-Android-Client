package com.desarrollodroide.pagekeeper.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.ui.components.ResponsiveContent
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Layout on windows wider than a portrait phone: landscape, tablets, unfolded foldables.
 *
 * Both cases below shipped broken and neither showed up on a portrait phone, which is why they
 * are pinned here rather than left to a visual check.
 */
@RunWith(AndroidJUnit4::class)
class AdaptiveLayoutTest {

    @get:Rule
    val rule = createComposeRule()

    private val noopActions = BookmarkActions(
        onClickEdit = {},
        onClickDelete = {},
        onClickShare = {},
        onClickCategory = {},
        onClickBookmark = {},
        onClickEpub = {},
        onClickSync = {},
    )

    /**
     * The card's hero image is 16:9 of the available width. In landscape the available width is the
     * long edge, so on a 1080p phone one image measured about 1350px tall on a 1080px screen: a
     * single card filled the viewport and the feed looked empty. A heightIn cap keeps the card a
     * card no matter how wide the window is.
     *
     * At 900dp an uncapped image alone is ~506dp, so a card that still fits in 560dp with its
     * title, excerpt, tags and action row can only be one whose image is capped.
     */
    @Test
    fun bookmarkCardHeightDoesNotScaleWithWindowWidth() {
        rule.setContent {
            ShioriTheme {
                Column(modifier = Modifier.requiredWidth(900.dp).testTag("card")) {
                    BookmarkItem(
                        getBookmark = { Bookmark.mock().copy(id = 1) },
                        serverURL = "",
                        xSessionId = "",
                        token = "",
                        actions = noopActions,
                        viewType = BookmarkViewType.FULL,
                    )
                }
            }
        }
        val bounds = rule.onNodeWithTag("card").getUnclippedBoundsInRoot()
        val height = bounds.bottom - bounds.top
        assertTrue("card was $height tall in a 900dp window", height < 560.dp)
    }

    /**
     * A tablet window is around 1280dp. Left to fill it, a form's fields and buttons span the whole
     * screen. The first attempt at a cap did nothing because the modifiers were the wrong way
     * round: fillMaxWidth().widthIn(max) pins the width to the parent's max before the cap is ever
     * consulted. It has to be widthIn(max).fillMaxWidth().
     */
    @Test
    fun responsiveContentCapsItsChildOnAWideWindow() {
        rule.setContent {
            ShioriTheme {
                ResponsiveContent(
                    modifier = Modifier.requiredWidth(1200.dp),
                    maxWidth = 460.dp,
                ) {
                    Box(modifier = Modifier.fillMaxWidth().testTag("content"))
                }
            }
        }
        rule.onNodeWithTag("content").assertWidthIsEqualTo(460.dp)
    }

    /** On a phone the cap is never reached, so content still uses the whole window. */
    @Test
    fun responsiveContentFillsANarrowWindow() {
        rule.setContent {
            ShioriTheme {
                ResponsiveContent(
                    modifier = Modifier.requiredWidth(320.dp),
                    maxWidth = 460.dp,
                ) {
                    Box(modifier = Modifier.fillMaxWidth().testTag("content"))
                }
            }
        }
        rule.onNodeWithTag("content").assertWidthIsEqualTo(320.dp)
    }

    /**
     * The hero image keeps its height however wide the card is.
     *
     * It used to be heightIn(240) plus aspectRatio(16:9), which cannot both be satisfied once a
     * card is wider than about 430dp: the image drew taller than the row the column had reserved
     * and the title rendered on top of the picture. Nothing was wrong on a phone, or in the three
     * column tablet grid, because the columns were narrow. It appeared the moment a single column
     * filled half a tablet in the two pane layout.
     */
    @Test
    fun theHeroImageKeepsItsHeightOnAWideCard() {
        rule.setContent {
            ShioriTheme {
                Column(modifier = Modifier.requiredWidth(900.dp)) {
                    BookmarkItem(
                        getBookmark = { Bookmark.mock().copy(id = 1) },
                        serverURL = "",
                        xSessionId = "",
                        token = "",
                        actions = noopActions,
                        viewType = BookmarkViewType.FULL,
                    )
                }
            }
        }

        rule.onNodeWithContentDescription("Bookmark image", useUnmergedTree = true)
            .assertHeightIsEqualTo(200.dp)
    }

    /** The same height on a phone width card, so the two look like the same component. */
    @Test
    fun theHeroImageKeepsItsHeightOnANarrowCard() {
        rule.setContent {
            ShioriTheme {
                Column(modifier = Modifier.requiredWidth(411.dp)) {
                    BookmarkItem(
                        getBookmark = { Bookmark.mock().copy(id = 1) },
                        serverURL = "",
                        xSessionId = "",
                        token = "",
                        actions = noopActions,
                        viewType = BookmarkViewType.FULL,
                    )
                }
            }
        }

        rule.onNodeWithContentDescription("Bookmark image", useUnmergedTree = true)
            .assertHeightIsEqualTo(200.dp)
    }
}
