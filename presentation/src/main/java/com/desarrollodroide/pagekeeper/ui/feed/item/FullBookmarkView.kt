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
    actions: BookmarkActions,
    isRefreshing: Boolean = false,
    hasSettledEmpty: Boolean = false,
) {
    val bookmark by remember { derivedStateOf(getBookmark) }
    val isRtl by remember {
        derivedStateOf { bookmark.title.isRTLText() || bookmark.excerpt.isRTLText() }
    }
    val imageUrl by remember {
        derivedStateOf { "${serverURL.removeTrailingSlash()}${bookmark.imageURL}" }
    }

    Column {
        // hasSettledEmpty: the server has already been asked and had nothing. Saying it is
        // still being fetched after that would be the aaaa.pd banner all over again.
        if (bookmark.isPendingServerProcessing && !hasSettledEmpty) {
            PendingSyncBanner(
                onRefresh = { actions.onClickRefresh(getBookmark) },
                isRefreshing = isRefreshing,
            )
        }
        if (bookmark.imageURL.isEmpty()) {
            // The server had no thumbnail. The slot is filled anyway, both so an image-less card
            // is not 200dp shorter than the one beside it and so the feed does not look like it
            // failed to load something.
            BookmarkImagePlaceholder(
                url = bookmark.url,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(HeroImageHeight)
                    .clip(MaterialTheme.shapes.medium),
            )
        } else {
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
                // Always 4dp. This used to open up to 16dp when there was no image, because back
                // then there was no hero at all and the title would otherwise start against the
                // card's edge. There is always a hero now, so the wider gap just made a card with
                // a placeholder 12dp taller than the one beside it.
                top = 4.dp,
                bottom = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                // Nothing here reserves space it is not using. The feed is a staggered grid, so
                // a card has no neighbour to line up with: every line held open for symmetry was
                // simply blank. Between them the title's second line, three excerpt lines and an
                // empty tag row came to about 130dp of nothing on a bookmark with no excerpt and
                // no tags, which is what a freshly added link is.
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = bookmark.title.ifEmpty { bookmark.url },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                if (bookmark.excerpt.isNotEmpty()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = bookmark.excerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3
                    )
                }
            }
            Text(
                text = bookmark.modified,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            // One chip row when there are tags, nothing at all when there are not. Reserving the
            // row on every card added 32dp of blank to most of them. Chips that do not fit are
            // still dropped rather than wrapping onto a second row.
            if (bookmark.tags.isNotEmpty()) {
                Box(modifier = Modifier.height(TagRowHeight)) {
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
