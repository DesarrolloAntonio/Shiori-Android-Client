package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.desarrollodroide.data.helpers.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun VisualSection(
    themeMode: MutableStateFlow<ThemeMode>,
    dynamicColors: MutableStateFlow<Boolean>,
    modifier: Modifier = Modifier,
) {
    SettingsGroup(title = "Visual", modifier = modifier) {
        ThemeOption(
            item = Item("Theme", Icons.Filled.Palette, onClick = {}),
            initialThemeMode = themeMode
        )
        val dynamicColorItem = Item(
            title = "Use dynamic colors",
            icon = Icons.Filled.FormatColorFill,
            switchState = dynamicColors
        )
        SwitchOption(
            item = dynamicColorItem,
            switchState = dynamicColors
        )
    }
}

@Composable
fun ThemeOption(
    item: Item,
    initialThemeMode: MutableStateFlow<ThemeMode>,
    modifier: Modifier = Modifier,
) {
    val themeMode by initialThemeMode.collectAsStateWithLifecycle()
    val themeIcon = when (themeMode) {
        ThemeMode.DARK -> Icons.Filled.DarkMode
        ThemeMode.LIGHT -> Icons.Filled.LightMode
        ThemeMode.AUTO -> Icons.Filled.HdrAuto
    }

    ListItem(
        modifier = modifier.clickable {
            initialThemeMode.value = when (themeMode) {
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.LIGHT -> ThemeMode.AUTO
                ThemeMode.AUTO -> ThemeMode.DARK
            }
            item.onClick()
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        headlineContent = { Text(item.title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(themeMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
        trailingContent = { Icon(themeIcon, contentDescription = null) },
    )
}
