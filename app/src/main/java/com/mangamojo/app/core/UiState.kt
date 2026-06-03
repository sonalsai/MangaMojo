package com.mangamojo.app.core

/**
 * Generic, immutable UI state for screens that load a single payload.
 * Screens decide how to render an empty payload (e.g. empty list) inside
 * [Success]. Sealed so `when` blocks stay exhaustive.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}

inline fun <T> UiState<T>.onSuccess(block: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) block(data)
    return this
}
