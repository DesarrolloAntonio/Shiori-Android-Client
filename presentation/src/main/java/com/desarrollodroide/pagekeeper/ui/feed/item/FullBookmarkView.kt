package com.desarrollodroide.pagekeeper.ui.feed.item

import com.desarrollodroide.pagekeeper.extensions.asLocalBookmarkDate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.pagekeeper.extensions.isRTLText
import com.desarrollodroide.pagekeeper.ui.components.LocalFeedInListPane

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
    // Read on every recomposition, not remembered. The feed keys a card on id and modified, and
    // Check brings back the scraped copy under the same id and the same modified (Shiori does not
    // touch it when it scrapes), so the card is recomposed rather than recreated. A remembered
    // derivedStateOf held the first bookmark it saw: Room had the title, the card kept the url.
    val bookmark = getBookmark()
    val isRtl = bookmark.title.isRTLText() || bookmark.excerpt.isRTLText()
    val imageUrl = "${serverURL.removeTrailingSlash()}${bookmark.imageURL}"

    Column {
        // hasSettledEmpty: the server has already been asked and had nothing. Saying it is
        // still being fetched after that would be the aaaa.pd banner all over again.
        if (bookmark.isPendingServerProcessing && !hasSettledEmpty) {
            PendingSyncBanner(
                onRefresh = { actions.onClickRefresh(getBookmark) },
                isRefreshing = isRefreshing,
            )
        }
        // A fixed height rather than an aspect ratio. Variable height heroes made the feed jump
        // around as each image resolved, and heightIn + aspectRatio only looked like a fix: past
        // about 430dp of card width the two cannot both be satisfied and the title rendered on top
        // of the picture. One number, the same at every width, with or without an image.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(HeroImageHeight)
                .clip(MaterialTheme.shapes.large),
        ) {
            if (bookmark.imageURL.isEmpty()) {
                // The server had no thumbnail. The slot is filled anyway, so an image-less card is
                // not 200dp shorter than the one beside it and the feed does not look broken.
                BookmarkImagePlaceholder(
                    url = bookmark.url,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                BookmarkImageView(
                    imageUrl = imageUrl,
                    xSessionId = xSessionId,
                    token = token,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loadAsThumbnail = false
                )
            }
            if (bookmark.hasArchive) {
                ArchivedBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Where it is from and when, above the title: it is what tells two similar titles
            // apart, and the date no longer needs a line of its own under the excerpt.
            Text(
                text = bookmarkMeta(bookmark.url, bookmark.modified.asLocalBookmarkDate()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = bookmark.title.ifEmpty { bookmark.url },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                // No excerpt beside an open article: the pane on the right shows the same text in
                // full, so in the list it would be duplication in the narrowest column on screen.
                if (bookmark.excerpt.isNotEmpty() && !LocalFeedInListPane.current) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = bookmark.excerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }
            }
            // One chip row when there are tags, nothing at all when there are not. Chips that do
            // not fit are dropped rather than wrapping onto a second row.
            if (bookmark.tags.isNotEmpty()) {
                Box(modifier = Modifier.height(TagRowHeight)) {
                    ClickableCategoriesView(
                        uniqueCategories = bookmark.tags,
                        onClickCategory = actions.onClickCategory
                    )
                }
            }
            ButtonsView(
                getBookmark = getBookmark,
                actions = actions,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** "Archived", over the cover, when the server keeps an offline copy of the page. */
@Composable
private fun ArchivedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Archive,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(text = "Archived", style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** "example.com · 19 Nov 2026": the site and the date, in one line. */
internal fun bookmarkMeta(url: String, date: String): String {
    val host = hostOf(url)
    return when {
        host.isEmpty() -> date
        date.isEmpty() -> host
        else -> "$host · $date"
    }
}

/** Hero image height. Fixed so a card is the same shape on a phone and in a half width pane. */
private val HeroImageHeight = 200.dp

/**
 * One AssistChip with its touch target. The chip draws 32dp but keeps a 48dp minimum interactive
 * size, and the one-line FlowRow drops a line that does not fit: at 32dp the row came out empty
 * and no card showed its tags.
 */
private val TagRowHeight = 48.dp
