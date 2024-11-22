package com.desarrollodroide.data.helpers

import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource

interface CrashHandler {
    fun initialize()

    companion object {
        fun create(settingsPreferenceDataSource: SettingsPreferenceDataSource): CrashHandler {
            return CrashHandlerImpl(settingsPreferenceDataSource).also { handler ->
                Thread.setDefaultUncaughtExceptionHandler(handler)
            }
        }
    }
}