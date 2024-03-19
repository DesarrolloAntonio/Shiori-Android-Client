package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import okhttp3.Headers

@Composable
fun BookmarkImageView(
    imageUrl: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .headers(
                if (isLegacyApi) {
                    Headers.Builder().add("X-Session-Id", xSessionId).build()
                } else {
                    Headers.Builder().add("Authorization", "Bearer $token").build()
                }
            )
            .build(),
        contentDescription = "Bookmark image",
        contentScale = contentScale,
        modifier = modifier
            .heightIn(max = 200.dp)
            .fillMaxWidth()
    )
}

