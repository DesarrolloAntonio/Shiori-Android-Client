package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.request.ImageRequest
import okhttp3.Headers
import android.graphics.Bitmap
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalInspectionMode
import coil.ImageLoader
import coil.size.Size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.FilterQuality
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import org.koin.androidx.compose.get

@Composable
fun BookmarkImageView(
    imageUrl: String,
    xSessionId: String,
    token: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
    loadAsThumbnail: Boolean
) {
    if (LocalInspectionMode.current) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Placeholder image",
            modifier = modifier
        )
    } else {
        val context = LocalContext.current
        val imageLoader = get<ImageLoader>()

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .apply {
                    if (loadAsThumbnail) {
                        size(Size(100, 100))
                    } else {
                        size(Size.ORIGINAL)
                    }
                }
                .headers(
                    Headers.Builder().add("Authorization", "Bearer $token").build()
                )
                .build(),
            contentDescription = "Bookmark image",
            imageLoader = imageLoader,
            modifier = modifier
                .heightIn(max = if (loadAsThumbnail) 100.dp else 200.dp)
                .fillMaxWidth(),
            alignment = Alignment.Center,
            contentScale = contentScale,
            alpha = 1.0f,
            colorFilter = null,
            filterQuality = FilterQuality.Medium,
            clipToBounds = true
        )
    }
}

