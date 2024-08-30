package com.desarrollodroide.pagekeeper

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import coil.ImageLoader
import coil.disk.DiskCache
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.navigation.Navigation
import org.koin.android.ext.android.inject
import com.desarrollodroide.pagekeeper.extensions.shareEpubFile
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageLoader = ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .build()
            }
            .build()

        logCoilCacheDetails(imageLoader)

        //val context = this.updateLocale(Locale("iw"))
        setContent {
            ComposeSetup(themeManager = themeManager) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Navigation(
                        onFinish = {
                            finish()
                        },
                        openUrlInBrowser = {
                            openUrlInBrowser(it)
                        },
                        shareEpubFile = {
                            shareEpubFile(it)
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.v("MainActivity", "onResume")
        // TODO: sync
    }
}

fun Context.updateLocale(locale: Locale): Context {
    Locale.setDefault(locale)
    val resources = this.resources
    val config = resources.configuration
    config.setLocale(locale)
    return this.createConfigurationContext(config)
}

fun logCoilCacheDetails(imageLoader: ImageLoader) {
    val diskCache = imageLoader.diskCache
    diskCache?.let {
        val cacheDir = it.directory.toFile()
        val files = cacheDir.listFiles()
        val imageCount = files?.size ?: 0
        Log.d("CoilCacheInfo", "Total images in disk cache: $imageCount")

        files?.forEach { file ->
            Log.d("CoilCacheInfo", "Cached file: ${file.name}")
        }
    } ?: run {
        Log.d("CoilCacheInfo", "No disk cache configured")
    }
}


