package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.ui.platform.LocalContext
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
import org.koin.compose.koinInject

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
        val imageLoader = koinInject<ImageLoader>()

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                // No size() call for the full size case on purpose. It used to ask for
                // Size.ORIGINAL, which decodes at the image's intrinsic resolution however large
                // that is, while the view only ever showed a couple of hundred dp of it. A big
                // server thumbnail decoded to a 164MB bitmap and Canvas refused to draw it:
                //   RuntimeException: Canvas: trying to draw too large bitmap
                // Leaving it unset lets Coil measure the target and downsample to it.
                .apply {
                    if (loadAsThumbnail) {
                        size(Size(THUMBNAIL_PX, THUMBNAIL_PX))
                    }
                }
                .headers(
                    Headers.Builder().add("Authorization", "Bearer $token").build()
                )
                .build(),
            contentDescription = "Bookmark image",
            imageLoader = imageLoader,
            // Sizing belongs to the caller. The view used to append heightIn + fillMaxWidth,
            // which overrode the 72dp box the compact row asks for.
            modifier = modifier,
            alignment = Alignment.Center,
            contentScale = contentScale,
            alpha = 1.0f,
            colorFilter = null,
            filterQuality = FilterQuality.Medium,
            clipToBounds = true
        )
    }
}

/** Decode hint for the compact row's thumbnail, in pixels, generous enough for xxhdpi. */
private const val THUMBNAIL_PX = 240
