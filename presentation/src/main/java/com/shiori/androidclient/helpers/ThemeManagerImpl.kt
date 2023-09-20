package com.shiori.androidclient.helpers

import androidx.compose.runtime.mutableStateOf
import com.shiori.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ThemeManagerImpl(
    settingsRepository: SettingsRepository,
) : ThemeManager {
    override var darkTheme = mutableStateOf(settingsRepository.isDarkTheme())
}