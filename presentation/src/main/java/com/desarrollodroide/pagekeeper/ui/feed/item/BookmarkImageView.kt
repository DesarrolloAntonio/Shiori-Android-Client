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
import android.graphics.ImageDecoder
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.ErrorResult
import com.desarrollodroide.pagekeeper.R
import okhttp3.OkHttpClient
import okio.IOException

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
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(id = R.drawable.bookmark_preview1),
            contentDescription = "Placeholder image",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        val context = LocalContext.current
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                val newCacheControl = "public, max-age=31536000"
                response.newBuilder()
                    .header("Cache-Control", newCacheControl)
                    .build()
            }
            .build()
        val imageLoader = ImageLoader.Builder(context)
            .okHttpClient { okHttpClient }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024 ) // 250MB
                    .build()
            }
            .build()
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .crossfade(100)
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
            imageLoader = imageLoader,
            loading = {
                //CircularProgressIndicator()
            },
            error = { state ->
                val errorResult = state.result as ErrorResult
                val throwable = errorResult.throwable
                Log.e("BookmarkImageView", "Error loading image: ${errorResult.request.data}")
                Log.e("BookmarkImageView", "Error type: ${throwable.javaClass.simpleName}")
                Log.e("BookmarkImageView", "Error message: ${throwable.message}")
                Log.e("BookmarkImageView", "Stack trace: ${throwable.stackTraceToString()}")

                Icon(
                    imageVector = Icons.Outlined.BrokenImage,
                    contentDescription = "Error loading image"
                )
            }
        )
    }
}

