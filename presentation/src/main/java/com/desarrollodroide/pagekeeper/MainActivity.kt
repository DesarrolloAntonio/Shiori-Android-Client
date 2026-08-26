package com.desarrollodroide.pagekeeper

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.navigation.Navigation
import org.koin.android.ext.android.inject
import com.desarrollodroide.pagekeeper.extensions.shareEpubFile
import com.desarrollodroide.pagekeeper.extensions.shareText
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorActivity
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars and let them take their colour from the content underneath.
        // This replaces the old `window.statusBarColor` write in ShioriTheme, which is deprecated
        // and a no-op from Android 15 onwards.
        enableEdgeToEdge()
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
                        },
                        onAddManuallyClick = {
                            startActivity(BookmarkEditorActivity.createManualIntent(this))
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO: sync when endpoint is available
    }
}

fun Context.updateLocale(locale: Locale): Context {
    Locale.setDefault(locale)
    val resources = this.resources
    val config = resources.configuration
    config.setLocale(locale)
    return this.createConfigurationContext(config)
}


