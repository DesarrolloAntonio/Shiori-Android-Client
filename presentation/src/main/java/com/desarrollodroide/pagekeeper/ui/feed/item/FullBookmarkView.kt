package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.model.Bookmark

@Composable
fun FullBookmarkView(
    bookmark: Bookmark,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    actions: BookmarkActions
) {
    val imageUrl =
        "${serverURL.removeTrailingSlash()}${bookmark.imageURL}?lastUpdated=${bookmark.modified}"
    Column {
        if (bookmark.imageURL.isNotEmpty()){
            BookmarkImageView(
                imageUrl = imageUrl,
                xSessionId = xSessionId,
                isLegacyApi = isLegacyApi,
                token = token,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    ),
                contentScale = ContentScale.FillWidth,
                loadAsThumbnail = false
            )
        }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = bookmark.title,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            Text(
                text = bookmark.modified,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
            Text(
                text = bookmark.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            ClickableCategoriesView(
                uniqueCategories = bookmark.tags,
                onClickCategory = actions.onClickCategory
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonsView(bookmark = bookmark, actions = actions)
        }
    }
}
