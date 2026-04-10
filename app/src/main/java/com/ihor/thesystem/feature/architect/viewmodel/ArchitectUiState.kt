package com.ihor.thesystem.feature.architect.viewmodel

import com.ihor.thesystem.domain.model.ChatMessage

/**
 * Описує стан екрану AI Архітектора у форматі чату.
 */
data class ArchitectUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val lastWorkoutContext: String? = null,
    val analysisAlreadySent: Boolean = false
)
