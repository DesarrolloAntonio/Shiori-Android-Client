package com.desarrollodroide.data.helpers

import android.util.Log
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandlerImpl(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
) : Thread.UncaughtExceptionHandler, CrashHandler {

    private val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun initialize() {
        Thread.setDefaultUncaughtExceptionHandler(this)
        Log.d("CrashHandler", "Initialized")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val stackTrace = throwable.stackTraceToString()

            val crashLog = buildString {
                appendLine("Timestamp: $timestamp")
                appendLine("Thread: ${thread.name}")
                appendLine("Exception: ${throwable.javaClass.name}")
                appendLine("Message: ${throwable.message}")
                appendLine("\nStack trace:")
                appendLine(stackTrace)
            }

            Log.d("CrashHandler", "Saving crash: $crashLog")

            coroutineScope.launch {
                try {
                    settingsPreferenceDataSource.setLastCrashLog(crashLog)
                    Log.d("CrashHandler", "Crash saved successfully")

                    // Verificar inmediatamente que se guardó
                    val saved = settingsPreferenceDataSource.getLastCrashLog()
                    Log.d("CrashHandler", "Verified saved crash: $saved")
                } catch (e: Exception) {
                    Log.e("CrashHandler", "Error saving crash", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CrashHandler", "Error in uncaughtException", e)
        }

        previousHandler?.uncaughtException(thread, throwable)
    }
}