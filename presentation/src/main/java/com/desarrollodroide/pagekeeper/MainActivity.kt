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
import com.desarrollodroide.pagekeeper.extensions.logCacheDetails
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.navigation.Navigation
import org.koin.android.ext.android.inject
import com.desarrollodroide.pagekeeper.extensions.shareEpubFile
import com.desarrollodroide.pagekeeper.extensions.shareText
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        shareText = {
                            shareText(it)
                        }
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


