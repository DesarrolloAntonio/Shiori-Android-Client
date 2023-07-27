package com.shiori.androidclient.ui.components

import kotlinx.coroutines.flow.MutableStateFlow

data class UiState<T>(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: T? = null,
    val idle: Boolean = true
)

fun <T> UiState<T>.success(data: T) = copy(isLoading = false, data = data, error = null, idle = false)

fun <T> UiState<T>.error(error: String) = copy(isLoading = false, data = null, error = error, idle = false)


fun <T> MutableStateFlow<UiState<T>>.success(data: T?) {
    value = value.copy(isLoading = false, data = data, error = null, idle = false)
}

fun <T> MutableStateFlow<UiState<T>>.error(errorMessage: String) {
    value = value.copy(isLoading = false, data = null, error = errorMessage, idle = false)
}

fun <T> MutableStateFlow<UiState<T>>.isLoading(isLoading: Boolean) {
    value = value.copy(isLoading = isLoading, data = null, error = null, idle = false)
}

fun <T> MutableStateFlow<UiState<T>>.idle(isIdle: Boolean) {
    value = value.copy(isLoading = false, data = null, error = null, idle = isIdle)
}

fun <T> MutableStateFlow<UiState<T>>.update(transform: (UiState<T>) -> UiState<T>) {
    value = transform(value)
}