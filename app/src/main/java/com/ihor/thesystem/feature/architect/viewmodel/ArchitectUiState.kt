package com.ihor.thesystem.feature.architect.viewmodel

import com.ihor.thesystem.domain.model.WorkoutDirective

/**
 * Описує можливі стани екрану (завантаження, успішне отримання даних, помилка).
 */
sealed interface ArchitectUiState<out T> {
    data object Loading : ArchitectUiState<Nothing>
    data class Success<T>(val data: T) : ArchitectUiState<T>
    data class Error(val message: String) : ArchitectUiState<Nothing>
}

/**
 * Модель даних, спеціально підготовлена для відображення на екрані Архітектора.
 */
data class ArchitectReportUiModel(
    val feedback: String,
    val stageStatus: String,
    val completedExercises: List<String>,
    val pendingExercises: List<String>,
    val directives: List<WorkoutDirective>,
    val recoveryHours: Double,
    val isFallback: Boolean
)