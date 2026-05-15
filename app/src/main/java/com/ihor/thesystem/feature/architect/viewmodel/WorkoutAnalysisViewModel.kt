package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.WorkoutAnalysisData
import com.ihor.thesystem.domain.usecase.GetWorkoutAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutAnalysisViewModel @Inject constructor(
    private val getWorkoutAnalysis: GetWorkoutAnalysisUseCase,
    private val dispatchers: DispatcherProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<WorkoutAnalysisData?>>(UiState.Loading)
    val uiState: StateFlow<UiState<WorkoutAnalysisData?>> = _uiState.asStateFlow()
    private val routeSessionId = savedStateHandle.get<Long>(SESSION_ID_ARG)?.takeIf { it > 0L }

    init {
        loadAnalysis()
    }

    fun loadAnalysis() {
        _uiState.value = UiState.Loading
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                routeSessionId?.let { getWorkoutAnalysis(it) } ?: getWorkoutAnalysis()
            }
                .onSuccess { analysis ->
                    _uiState.value = UiState.Content(analysis)
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.value = UiState.Error(
                        UiText.StringResource(R.string.error_workout_analysis_failed)
                    )
                }
        }
    }
}

private const val SESSION_ID_ARG = "sessionId"
