package com.desarrollodroide.pagekeeper.ui.components

import kotlinx.coroutines.flow.MutableStateFlow

data class UiState<T>(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val data: T? = null,
    val idle: Boolean = true
)

fun <T> UiState<T>.success(data: T) = copy(isLoading = false, data = data, error = null, idle = false, isUpdating = false)

fun <T> UiState<T>.error(error: String) = copy(isLoading = false, data = null, error = error, idle = false, isUpdating = false)


fun <T> MutableStateFlow<UiState<T>>.success(data: T?) {
    value = value.copy(isLoading = false, data = data, error = null, idle = false, isUpdating = false)
}

fun <T> MutableStateFlow<UiState<T>>.error(errorMessage: String) {
    value = value.copy(isLoading = false, data = null, error = errorMessage, idle = false, isUpdating = false)
}

fun <T> MutableStateFlow<UiState<T>>.isLoading(isLoading: Boolean) {
    value = value.copy(isLoading = isLoading, data = null, error = null, idle = false, isUpdating = false)
}

fun <T> MutableStateFlow<UiState<T>>.idle(isIdle: Boolean) {
    value = value.copy(isLoading = false, data = null, error = null, idle = isIdle, isUpdating = false)
}

fun <T> MutableStateFlow<UiState<T>>.isUpdating(isUpdating: Boolean) {
    value = value.copy(isLoading = false, data = value.data, error = null, idle = false, isUpdating = isUpdating)
}