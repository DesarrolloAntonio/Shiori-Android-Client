package com.desarrollodroide.common.result

data class NetworkLogEntry(
    val timestamp: String,
    val priority: String, // "I" for Info (request), "S" for Success (response), "E" for Error
    val url: String,
    val message: String
)