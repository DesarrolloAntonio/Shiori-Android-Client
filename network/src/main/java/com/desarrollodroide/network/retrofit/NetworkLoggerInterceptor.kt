package com.desarrollodroide.network.retrofit

import com.desarrollodroide.common.result.NetworkLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.asStateFlow

class NetworkLoggerInterceptor : Interceptor {
    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs.asStateFlow()

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun addLog(entry: NetworkLogEntry) {
        _logs.update { currentLogs -> currentLogs + entry }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val timestamp = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
            .format(startTime)

        // Log request
        addLog(
            NetworkLogEntry(
                timestamp = timestamp,
                priority = "I",
                url = request.url.toString(),
                message = "${request.method} ${request.url.encodedPath}"
            )
        )

        return try {
            chain.proceed(request).also { response ->
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime

                // Log response
                addLog(
                    NetworkLogEntry(
                        timestamp = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
                            .format(endTime),
                        priority = if (response.isSuccessful) "S" else "E",
                        url = request.url.toString(),
                        message = "HTTP ${response.code} (${duration}ms)\n" +
                                response.peekBody(1024).string()
                    )
                )
            }
        } catch (e: Exception) {
            addLog(
                NetworkLogEntry(
                    timestamp = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
                        .format(System.currentTimeMillis()),
                    priority = "E",
                    url = request.url.toString(),
                    message = e.message ?: "Unknown error"
                )
            )
            throw e
        }
    }
}