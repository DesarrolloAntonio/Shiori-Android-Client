package com.desarrollodroide.pagekeeper.helpers

import androidx.compose.runtime.mutableStateOf
import com.desarrollodroide.data.repository.SettingsRepository

class ThemeManagerImpl(
    settingsRepository: SettingsRepository,
) : ThemeManager {
    override var darkTheme = mutableStateOf(settingsRepository.isDarkTheme())
}