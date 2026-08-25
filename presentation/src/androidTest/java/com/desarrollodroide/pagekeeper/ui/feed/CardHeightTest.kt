package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkActions
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkItem
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Card heights in the feed.
 *
 * The feed is a staggered grid, so cards are meant to differ: each is as tall as its own content
 * and the columns pack. What must not differ is a card that happens to have no thumbnail, which
 * used to draw no hero at all and come out 200dp short. The placeholder fills that slot, and this
 * pins it down.
 */
@RunWith(AndroidJUnit4::class)
class CardHeightTest {

    @get:Rule
    val rule = createComposeRule()

    // Identical but for the thumbnail, which is the whole point: one gets an image, the other
    // gets the placeholder, and they have to come out the same height.
    private val withImage = Bookmark.mock().copy(
        id = 1,
        imageURL = "/bookmark/1/thumb",
        title = "A title long enough to wrap onto a second line in a narrow column",
        excerpt = "An excerpt with enough words in it to run to three full lines of body text, " +
            "which is the most the card will show before it starts to ellipsise the rest away.",
        tags = listOf(Tag(1, "android"), Tag(2, "compose")),
    )

    private val withoutImage = withImage.copy(id = 2, imageURL = "")

    private val noActions = BookmarkActions(
        onClickEdit = { },
        onClickDelete = { },
        onClickShare = { },
        onClickCategory = { },
        onClickBookmark = { },
        onClickEpub = { },
        onClickSync = { },
        onToggleSelection = { },
    )

    @Test
    fun aCardWithNoThumbnailIsNotShorterThanOneWithIt() {
        rule.setContent {
            ShioriTheme {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .requiredWidth(800.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(listOf(withImage, withoutImage)) { bookmark ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.testTag("card")
                        ) {
                            BookmarkItem(
                                getBookmark = { bookmark },
                                serverURL = "http://test",
                                xSessionId = "",
                                token = "",
                                viewType = BookmarkViewType.FULL,
                                actions = noActions,
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()

        val cards = rule.onAllNodesWithTag("card").fetchSemanticsNodes()
        assertEquals("both cards should be composed", 2, cards.size)

        val heights = rule.onAllNodesWithTag("card").let { nodes ->
            (0 until 2).map { index ->
                val bounds = nodes[index].getUnclippedBoundsInRoot()
                bounds.bottom - bounds.top
            }
        }

        val difference = abs(heights[0].value - heights[1].value)
        assertEquals(
            "a missing thumbnail must not shorten the card: they differ by ${difference}dp " +
                "(${heights[0]} vs ${heights[1]})",
            0f,
            difference,
            1f
        )
    }

    /**
     * Equal heights are worthless if they were bought by losing the content.
     *
     * A first attempt at this used fillMaxHeight on the card and a weighted spacer inside it. The
     * heights matched and this assertion is what the run on a device would have caught: inside a
     * lazy grid the item is measured with an unbounded height, so weight and fill have nothing
     * real to divide up, and the text and the action row disappeared off the card entirely.
     */
    @Test
    fun theCardKeepsItsContent() {
        rule.setContent {
            ShioriTheme {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .requiredWidth(800.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(listOf(withImage, withoutImage)) { bookmark ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.testTag("card")
                        ) {
                            BookmarkItem(
                                getBookmark = { bookmark },
                                serverURL = "http://test",
                                xSessionId = "",
                                token = "",
                                viewType = BookmarkViewType.FULL,
                                actions = noActions,
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText(withImage.title).assertCountEquals(2)
        assertEquals(
            "every card keeps its action row",
            2,
            rule.onAllNodesWithContentDescription("Delete").fetchSemanticsNodes().size
        )
    }
}
