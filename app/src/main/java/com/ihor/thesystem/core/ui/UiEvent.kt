package com.ihor.thesystem.core.ui

sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
}
