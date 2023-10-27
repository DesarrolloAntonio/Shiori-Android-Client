package com.desarrollodroide.pagekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.navigation.Navigation
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        }
                    )
                }
            }
        }
    }
}
