package com.desarrollodroide.pagekeeper.helpers

import androidx.compose.runtime.MutableState
import com.desarrollodroide.data.helpers.ThemeMode

interface ThemeManager {
    var themeMode: MutableState<ThemeMode>
    var useDynamicColors: MutableState<Boolean>
}