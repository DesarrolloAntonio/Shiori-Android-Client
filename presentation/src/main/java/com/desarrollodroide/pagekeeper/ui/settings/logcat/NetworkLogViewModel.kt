package com.desarrollodroide.pagekeeper.ui.settings.logcat

import androidx.lifecycle.ViewModel
import com.desarrollodroide.network.retrofit.NetworkLoggerInterceptor

class NetworkLogViewModel(
    private val logger: NetworkLoggerInterceptor,
) : ViewModel() {

    val logs = logger.logs

    fun clearLogs() {
        logger.clearLogs()
    }

    fun shareLogs() = logs.value.joinToString("\n") {
            "${it.timestamp} ${it.priority}/${it.url}: ${it.message}"
        }

}