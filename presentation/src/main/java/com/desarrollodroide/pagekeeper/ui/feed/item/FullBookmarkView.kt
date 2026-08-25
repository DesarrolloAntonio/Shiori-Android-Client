package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.pagekeeper.extensions.isRTLText

@Composable
fun FullBookmarkView(
    getBookmark: GetBookmark,
    serverURL: String,
    xSessionId: String,
    token: String,
    actions: BookmarkActions
) {
    val bookmark by remember { derivedStateOf(getBookmark) }
    val isRtl by remember {
        derivedStateOf { bookmark.title.isRTLText() || bookmark.excerpt.isRTLText() }
    }
    val imageUrl by remember {
        derivedStateOf { "${serverURL.removeTrailingSlash()}${bookmark.imageURL}" }
    }

    Column {
        if (bookmark.isPendingServerProcessing) {
            PendingSyncBanner(onRefresh = { actions.onClickRefresh(getBookmark) })
        }
        if (bookmark.imageURL.isNotEmpty()) {
            // A fixed height rather than an aspect ratio. Variable height heroes made the feed
            // jump around as each image resolved, and heightIn + aspectRatio only looked like a
            // fix: past about 430dp of card width the two cannot both be satisfied, the image
            // drew taller than the row the column had reserved for it, and the title rendered on
            // top of the picture. That width is reachable in the two pane layout, where a single
            // column fills half a tablet. One number, the same at every width.
            BookmarkImageView(
                imageUrl = imageUrl,
                xSessionId = xSessionId,
                token = token,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(HeroImageHeight)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                loadAsThumbnail = false
            )
        }
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = if (bookmark.imageURL.isNotEmpty()) 4.dp else 16.dp,
                bottom = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                // Fixed line counts, not "up to". A grid row is as tall as its tallest card, so
                // a card whose title runs to one line and whose excerpt runs to two just ends
                // higher than its neighbour and leaves a hole, with the action rows at different
                // levels. Reserving the lines costs a little blank space on short entries and
                // makes every card in a row end together.
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = bookmark.title.ifEmpty { bookmark.url },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2,
                    maxLines = 2
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = bookmark.excerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 3,
                    maxLines = 3
                )
            }
            Text(
                text = bookmark.modified,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            // The slot is always there, tags or not, and it is one chip row tall. Chips that do
            // not fit are dropped rather than wrapping onto a second row, which would make the
            // card taller than its neighbours again.
            Box(modifier = Modifier.height(TagRowHeight)) {
                if (bookmark.tags.isNotEmpty()) {
                    ClickableCategoriesView(
                        uniqueCategories = bookmark.tags,
                        onClickCategory = actions.onClickCategory
                    )
                }
            }
            ButtonsView(getBookmark = getBookmark, actions = actions)
        }
    }
}

/** Hero image height. Fixed so a card is the same shape on a phone and in a half width pane. */
private val HeroImageHeight = 200.dp

/** One AssistChip tall. Reserved whether or not the bookmark has tags, so cards stay level. */
private val TagRowHeight = 32.dp
