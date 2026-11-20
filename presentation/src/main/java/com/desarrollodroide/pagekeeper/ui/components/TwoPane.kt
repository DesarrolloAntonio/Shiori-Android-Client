package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.desarrollodroide.pagekeeper.ui.feed.EmptyIllustration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The window has to be this wide before the feed and an article are worth showing side by side.
 *
 * 840dp is the Material expanded breakpoint. Below it the two panes would be narrower than a phone
 * each, so the feed keeps the whole window and an article opens over it.
 */
val TwoPaneMinWidth: Dp = 840.dp

/**
 * Width of the list pane. Fixed, not half the window: the list is a column of cards and stops
 * gaining anything past roughly one card, while the article always reads better with more room.
 *
 * 340dp is what the feed's own grid already calls one card, so the pane holds exactly one column
 * and every dp beyond that was going to the list without the list doing anything with it.
 */
val ListPaneWidth: Dp = 340.dp

/**
 * Whether the feed is drawing inside the two-pane list rather than owning the window.
 *
 * A local rather than a parameter because it would otherwise be threaded through five signatures
 * between the navigation graph and the card, every one of which would pass it straight down.
 *
 * It settles one thing that only the layout above knows: the card drops its excerpt, because the
 * article that excerpt summarises is open in the next pane.
 */
val LocalFeedInListPane = staticCompositionLocalOf { false }

/**
 * Whether the feed and an article should sit side by side.
 *
 * Takes a width rather than a window size class so the same decision can be made from a test, and
 * so it reacts to the space this composable actually got rather than to the size of the window.
 */
fun shouldUseTwoPanes(preferenceEnabled: Boolean, availableWidth: Dp): Boolean =
    preferenceEnabled && availableWidth >= TwoPaneMinWidth

/**
 * What the detail pane shows until the reader has something to read: the same heap of shapes as
 * the empty library, around an article, so the two empty states look like one app.
 */
@Composable
fun TwoPaneEmptyDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyIllustration(icon = Icons.Outlined.Article)
        Text(
            modifier = Modifier.padding(top = 28.dp),
            text = "Nothing open",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.8).sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Pick a bookmark on the left to read it here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
