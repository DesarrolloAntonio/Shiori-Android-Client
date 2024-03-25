package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.Transformation
import okhttp3.Headers
import android.graphics.Bitmap
import coil.size.Size
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
import coil.request.Options

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
    val finalImageUrl = if (loadAsThumbnail) "$imageUrl?thumbnail=true" else imageUrl

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(finalImageUrl)
            .crossfade(true)
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

        },
        error = {

        }
    )
}

@Composable
fun BookmarkImageView3(
    imageUrl: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
    loadAsThumbnail: Boolean
) {
    val transformation = if (loadAsThumbnail) {
        listOf(object : Transformation {
            override val cacheKey: String
                get() = "thumbnailTransformation"

            override suspend fun transform(input: Bitmap, size: Size): Bitmap {
                val width = (input.width * 0.1).toInt()
                val height = (input.height * 0.1).toInt()
                return Bitmap.createScaledBitmap(input, width, height, true)
            }
        })
    } else {
        emptyList()
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .transformations(transformation)
            .apply {
                if (isLegacyApi) {
                    headers(Headers.Builder().add("X-Session-Id", xSessionId).build())
                } else {
                    headers(Headers.Builder().add("Authorization", "Bearer $token").build())
                }
            }
            .build(),
        contentDescription = "Bookmark image",
        contentScale = contentScale,
        modifier = modifier
            .heightIn(max = if (loadAsThumbnail) 100.dp else 200.dp)
            .fillMaxWidth(),
        loading = {
            // Add your loading UI here
        },
        error = {
            // Add your error UI here
        }
    )
}

