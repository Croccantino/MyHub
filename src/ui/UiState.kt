package com.myhub.app.ui

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String = "PC offline") : UiState<Nothing>()
}