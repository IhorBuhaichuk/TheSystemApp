package com.ihor.thesystem.core.ui

sealed class UiEvent {
    data class ShowError(val uiText: UiText) : UiEvent()
}

/**
 * Exception that carries a string resource ID for UI display.
 */
class StringResourceException(val uiText: UiText) : Exception()
