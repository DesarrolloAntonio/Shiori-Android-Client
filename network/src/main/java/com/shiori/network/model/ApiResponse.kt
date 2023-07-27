package com.shiori.network.model


data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)


