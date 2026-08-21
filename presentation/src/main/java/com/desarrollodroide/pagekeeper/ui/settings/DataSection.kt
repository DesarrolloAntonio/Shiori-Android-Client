package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DataSection(
    cacheSize: StateFlow<String>,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentCacheSize by cacheSize.collectAsStateWithLifecycle()

    SettingsGroup(title = "Data", modifier = modifier) {
        ClickableOption(
            title = "Clear Image Cache",
            icon = Icons.Default.Delete,
            subtitle = "Cache size: $currentCacheSize",
            onClick = onClearCache
        )
    }
}
