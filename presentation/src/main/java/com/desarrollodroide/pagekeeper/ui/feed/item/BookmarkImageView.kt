package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.request.ImageRequest
import okhttp3.Headers
import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon

@Composable
fun BookmarkImageView(
    imageUrl: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
    loadAsThumbnail: Boolean
) {
    //val finalImageUrl = if (loadAsThumbnail) "$imageUrl?thumbnail=true" else imageUrl
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            //.crossfade(true)
            .apply {
                if (loadAsThumbnail) {
                    //size(10)
                }
            }
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
            .heightIn(max = if (loadAsThumbnail) 100.dp else 200.dp)
            .fillMaxWidth(),
        loading = {
            //CircularProgressIndicator()
        },
        error = {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = "Error loading image"
            )
        }
    )
}

