package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
            PendingSyncBanner()
        }
        if (bookmark.imageURL.isNotEmpty()) {
            // A fixed 16:9 crop instead of FillWidth: variable-height hero images made the feed
            // jump around as each one resolved.
            BookmarkImageView(
                imageUrl = imageUrl,
                xSessionId = xSessionId,
                token = token,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .aspectRatio(16f / 9f)
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
            if (bookmark.tags.isNotEmpty()) {
                ClickableCategoriesView(
                    uniqueCategories = bookmark.tags,
                    onClickCategory = actions.onClickCategory
                )
            }
            ButtonsView(getBookmark = getBookmark, actions = actions)
        }
    }
}
