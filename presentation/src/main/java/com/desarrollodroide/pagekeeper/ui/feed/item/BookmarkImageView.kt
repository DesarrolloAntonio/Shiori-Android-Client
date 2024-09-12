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
import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalInspectionMode
import coil.ImageLoader
import coil.disk.DiskCache
import coil.size.Size
import okhttp3.OkHttpClient
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.FilterQuality
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image

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
    if (LocalInspectionMode.current) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Placeholder image",
            modifier = modifier
        )
    } else {
        val context = LocalContext.current
        val imageLoader = ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val response = chain.proceed(request)
                        if (!response.isSuccessful) {
                            Log.e("BookmarkImageView", "HTTP error: ${response.code}")
                        }
                        val newCacheControl = "public, max-age=31536000"
                        response.newBuilder()
                            .header("Cache-Control", newCacheControl)
                            .build()
                    }
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024) // 250MB
                    .build()
            }
            .build()

        var retryHash by remember { mutableStateOf(0) }

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
                    if (isLegacyApi) {
                        Headers.Builder().add("X-Session-Id", xSessionId).build()
                    } else {
                        Headers.Builder().add("Authorization", "Bearer $token").build()
                    }
                )
                .setParameter("retry_hash", retryHash)
                .build(),
            contentDescription = "Bookmark image",
            imageLoader = imageLoader,
            modifier = modifier
                .heightIn(max = if (loadAsThumbnail) 100.dp else 200.dp)
                .fillMaxWidth(),
//            placeholder = rememberVectorPainter(Icons.Default.Image),
//            error = rememberVectorPainter(Icons.Default.BrokenImage),
//            fallback = rememberVectorPainter(Icons.Default.HideImage),
            onLoading = { state ->
                Log.d("BookmarkImageView", "Loading")
            },
            onSuccess = { state ->
                Log.d("BookmarkImageView", "Success: ${state.result}")
            },
            onError = { state ->
                Log.e("BookmarkImageView", "Error: ${state.result}")
                imageLoader.diskCache?.remove(imageUrl)
                retryHash++
            },
            alignment = Alignment.Center,
            contentScale = contentScale,
            alpha = 1.0f,
            colorFilter = null,
            filterQuality = FilterQuality.Medium,
            clipToBounds = true
        )
    }
}

