package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.helpers.ThemeMode

@Composable
fun VisualSection(
    themeMode: ThemeMode,
    onThemeChanged: (ThemeMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Text(text = "Visual", style = MaterialTheme.typography.titleSmall)

            ThemeOption(
                item = Item("Theme", Icons.Filled.Palette, onClick = {
                    val newMode = when (themeMode) {
                        ThemeMode.DARK -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.AUTO
                        ThemeMode.AUTO -> ThemeMode.DARK
                    }
                    onThemeChanged(newMode)
                }),
                initialThemeMode = themeMode
            )
        }

    }
}

@Composable
fun ThemeOption(
    item: Item,
    initialThemeMode: ThemeMode,
) {
    var themeMode by remember { mutableStateOf(initialThemeMode) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                themeMode = when (themeMode) {
                    ThemeMode.DARK -> ThemeMode.LIGHT
                    ThemeMode.LIGHT -> ThemeMode.AUTO
                    ThemeMode.AUTO -> ThemeMode.DARK
                }
                item.onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(item.icon, contentDescription = "Change theme")
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.title, modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        )

        val themeIcon = when (themeMode) {
            ThemeMode.DARK -> Icons.Filled.DarkMode
            ThemeMode.LIGHT -> Icons.Filled.LightMode
            ThemeMode.AUTO -> Icons.Filled.HdrAuto
        }
        Icon(themeIcon, contentDescription = "Change theme")
    }
}

