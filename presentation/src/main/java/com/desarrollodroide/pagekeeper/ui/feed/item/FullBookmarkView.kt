package com.desarrollodroide.pagekeeper.ui.feed.item

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.extensions.isRTLText

@Composable
fun FullBookmarkView(
    getBookmark: GetBookmark,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    actions: BookmarkActions
) {
    val bookmark by remember { derivedStateOf(getBookmark) }
    val isArabic by remember { derivedStateOf { bookmark.title.isRTLText() || bookmark.excerpt.isRTLText() } }
    val imageUrl by remember { derivedStateOf { "${serverURL.removeTrailingSlash()}${bookmark.imageURL}?lastUpdated=${bookmark.modified}" } }

    Column {
        if (bookmark.imageURL.isNotEmpty()) {
            BookmarkImageView(
                imageUrl = imageUrl,
                xSessionId = xSessionId,
                isLegacyApi = isLegacyApi,
                token = token,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.FillWidth,
                loadAsThumbnail = false
            )
        }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    text = bookmark.excerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
            }
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = bookmark.modified,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            ClickableCategoriesView(
                uniqueCategories = bookmark.tags,
                onClickCategory = actions.onClickCategory
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonsView(getBookmark = getBookmark, actions = actions)
        }
    }
}

@Preview
@Composable
private fun FullBookmarkViewPreview() {
    MaterialTheme {
        val mockBookmark = Bookmark.mock()
        val actions = BookmarkActions(
            onClickEdit = {},
            onClickDelete = {},
            onClickShare = {},
            onClickCategory = {},
            onClickBookmark = {},
            onClickEpub = {},
            onClickSync = {}
        )
        Column {
            BookmarkItem(
                getBookmark = { mockBookmark },
                serverURL = "",
                xSessionId = "",
                isLegacyApi = false,
                token = "",
                actions = actions,
                viewType = BookmarkViewType.FULL
            )
            BookmarkItem(
                getBookmark = { mockBookmark },
                serverURL = "",
                xSessionId = "",
                isLegacyApi = false,
                token = "",
                actions = actions,
                viewType = BookmarkViewType.SMALL
            )
        }
    }
}
