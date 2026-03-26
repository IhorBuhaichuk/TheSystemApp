package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.usecase.GetProgressionMatrixUseCase
import com.ihor.thesystem.domain.usecase.UpdateExerciseWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val playerRepo:      PlayerRepository,
    private val getMatrix:       GetProgressionMatrixUseCase,
    private val updateWeight:    UpdateExerciseWeightUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<StatisticsUiData>> =
        combine(
            playerRepo.getPlayer().filterNotNull(),
            getMatrix()
        ) { player, matrix ->
            StatisticsUiData(
                playerName      = player.name,
                playerClass     = player.playerClass,
                currentMonth    = player.currentMonth,
                currentWeek     = player.currentWeek,
                currentCycleDay = player.currentCycleDay,
                isPenaltyActive = player.isPenaltyActive,
                matrixEntries   = matrix.map { it.toUiModel() }
            )
        }
            .map<StatisticsUiData, UiState<StatisticsUiData>> { UiState.Content(it) }
            .catch { emit(UiState.Error(it.message ?: "Помилка")) }
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            )

    private val _dialogState =
        MutableStateFlow<StatisticsDialogState>(StatisticsDialogState.None)
    val dialogState: StateFlow<StatisticsDialogState> = _dialogState.asStateFlow()

    fun onEditWeightTap(entry: MatrixEntryUiModel) {
        _dialogState.value = StatisticsDialogState.EditWeight(entry)
    }

    fun onDismissDialog() {
        _dialogState.value = StatisticsDialogState.None
    }

    fun onWeightConfirmed(exerciseId: Int, newWeight: Float) {
        viewModelScope.launch {
            updateWeight(exerciseId, newWeight)
            onDismissDialog()
        }
    }
}

private fun ProgressionMatrixEntry.toUiModel() = MatrixEntryUiModel(
    exerciseId       = exerciseId,
    exerciseName     = exerciseName,
    startWeight      = startWeight,
    targetWeight     = targetWeight,
    currentWeight    = currentWeight,
    targetWeightNote = targetWeightNote,
    weeklyStep       = weeklyStep,
    progressPercent  = progressPercent
)