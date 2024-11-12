package com.desarrollodroide.pagekeeper.ui.settings.crash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrashLogViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
) : ViewModel() {

    private val _crashLog = MutableStateFlow<String?>(null)
    val crashLog = _crashLog.asStateFlow()

    init {
        loadLastCrash()
    }

    private fun loadLastCrash() {
        viewModelScope.launch {
            _crashLog.value = settingsPreferenceDataSource.getLastCrashLog()
        }
    }

    fun clearCrashLog() {
        viewModelScope.launch {
            settingsPreferenceDataSource.clearLastCrashLog()
            _crashLog.value = ""
        }
    }

    fun shareCrashLog(): String = crashLog.value ?: ""

}