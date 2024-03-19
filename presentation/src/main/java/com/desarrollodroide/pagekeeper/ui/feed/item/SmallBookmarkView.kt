package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Bookmark

@Composable
fun SmallBookmarkView(
    bookmark: Bookmark,
    imageUrl: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    actions: BookmarkActions
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .height(85.dp)
    ) {
        BookmarkImageView(
            imageUrl = imageUrl,
            xSessionId = xSessionId,
            isLegacyApi = isLegacyApi,
            token = token,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(
                    RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = bookmark.title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            ButtonsView(bookmark = bookmark, actions = actions)
        }
    }
}