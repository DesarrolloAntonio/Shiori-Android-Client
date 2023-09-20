package com.shiori.androidclient

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.shiori.androidclient.helpers.ThemeManager
import com.shiori.androidclient.ui.theme.ShioriTheme

@Composable
fun ComposeSetup(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    ShioriTheme(
        darkTheme = themeManager.darkTheme.value
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
