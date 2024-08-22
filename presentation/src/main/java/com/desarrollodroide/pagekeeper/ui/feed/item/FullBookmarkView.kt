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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.extensions.isRTLText

@Composable
fun FullBookmarkView(
    bookmark: Bookmark,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    actions: BookmarkActions
) {
    val isArabic = bookmark.title.isRTLText() || bookmark.excerpt.isRTLText()
    val imageUrl =
        "${serverURL.removeTrailingSlash()}${bookmark.imageURL}?lastUpdated=${bookmark.modified}"
    Log.v("imageUrl", imageUrl)
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
                    .clip(
                        RoundedCornerShape(12.dp)
                    ),
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
            ButtonsView(bookmark = bookmark, actions = actions)
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "RTL Layout",
    showBackground = true,
    showSystemUi = true,
    locale = "ar"
)
@Composable
fun BookmarkPreview() {
    val bookmark = Bookmark.mock()

    MaterialTheme {
        FullBookmarkView(
            bookmark = bookmark,
            serverURL = "https://example.com",
            xSessionId = "session-id",
            isLegacyApi = false,
            token = "token",
            actions = BookmarkActions(
                onClickCategory = { tag -> /* Handle category click */ },
                onClickBookmark = { /* Handle archive button click */ },
                onClickDelete = { /* Handle ebook button click */ },
                onClickEdit = { /* Handle delete button click */ },
                onClickEpub = { /* Handle epub button click */ },
                onClickShare = { /* Handle share button click */ },
                onClickSync = { /* Handle sync button click */ }
            )
        )
    }
}
