package com.desarrollodroide.pagekeeper.extensions

import android.util.Log
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoilApi::class)
fun ImageLoader.logCacheDetails() {
    diskCache?.let { cache ->
        val imageCount = cache.directory.toFile().listFiles()?.size ?: 0
        Log.d("CoilCacheInfo", "Total images in disk cache: $imageCount")
    } ?: Log.d("CoilCacheInfo", "No disk cache configured")
}

@OptIn(ExperimentalCoilApi::class)
suspend fun ImageLoader.clearCache() = withContext(Dispatchers.IO) {
    memoryCache?.clear()
    diskCache?.clear()
}