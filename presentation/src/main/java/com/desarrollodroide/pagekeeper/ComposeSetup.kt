package com.desarrollodroide.pagekeeper

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

@Composable
fun ComposeSetup(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeManager.themeMode.value) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }
    ShioriTheme(
        darkTheme = isDarkTheme
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
