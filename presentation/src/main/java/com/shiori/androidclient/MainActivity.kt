package com.shiori.androidclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shiori.androidclient.ui.list.ListScreen
import com.shiori.androidclient.ui.theme.ShioriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            ShioriTheme {
                ListScreen()
            }
        }
    }
}
