package com.desarrollodroide.pagekeeper.helpers

import androidx.compose.runtime.mutableStateOf
import com.desarrollodroide.data.repository.SettingsRepository

class ThemeManagerImpl(
    settingsRepository: SettingsRepository,
) : ThemeManager {
      override var themeMode = mutableStateOf(settingsRepository.getThemeMode())
      override var useDynamicColors = mutableStateOf(settingsRepository.getUseDynamicColors())
}