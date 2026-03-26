package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.usecase.FinalizeSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchitectViewModel @Inject constructor(
    private val finalizeSessionUseCase: FinalizeSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArchitectUiState<ArchitectReportUiModel>>(ArchitectUiState.Loading)
    val uiState: StateFlow<ArchitectUiState<ArchitectReportUiModel>> = _uiState.asStateFlow()

    /**
     * Запускає процес фіналізації: збереження в БД, запит до AI (або Fallback) та фільтрацію директив.
     */
    fun finalizeSession(
        session: WorkoutSession,
        sets: List<ExerciseSet>,
        isNightShift: Boolean = false
    ) {
        _uiState.value = ArchitectUiState.Loading

        viewModelScope.launch {
            val result = finalizeSessionUseCase(session, sets, isNightShift)

            result.fold(
                onSuccess = { report ->
                    val uiModel = ArchitectReportUiModel(
                        feedback = report.architectFeedback,
                        stageStatus = report.currentStageStatus,
                        completedExercises = report.completedExercises,
                        pendingExercises = report.pendingExercises,
                        directives = report.nextWorkoutDirectives,
                        recoveryHours = report.recoveryWindowHours,
                        isFallback = report.isFallback
                    )
                    _uiState.value = ArchitectUiState.Success(uiModel)
                },
                onFailure = { exception ->
                    _uiState.value = ArchitectUiState.Error(
                        exception.message ?: "Критична помилка при фіналізації сесії."
                    )
                }
            )
        }
    }

    /**
     * Скидає стан екрану, підтверджуючи ознайомлення з директивами.
     */
    fun acknowledge() {
        _uiState.value = ArchitectUiState.Loading
    }
}