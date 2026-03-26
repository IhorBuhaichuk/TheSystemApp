package com.ihor.thesystem.core.ui

sealed interface UiState<out T> {
    object Loading                     : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}