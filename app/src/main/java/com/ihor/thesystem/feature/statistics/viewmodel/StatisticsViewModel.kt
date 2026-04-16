package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.GetLogForDateUseCase
import com.ihor.thesystem.domain.usecase.GetStatisticsDataUseCase
import com.ihor.thesystem.domain.usecase.RecalculateGlobalRankUseCase
import com.ihor.thesystem.domain.usecase.SaveExerciseSetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val getStatisticsDataUseCase: GetStatisticsDataUseCase,
    private val getLogForDateUseCase: GetLogForDateUseCase,
    private val saveExerciseSetsUseCase: SaveExerciseSetsUseCase,
    private val recalculateGlobalRankUseCase: RecalculateGlobalRankUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<StatisticsUiData>> = getStatisticsDataUseCase()
        .map<StatisticsUiData, UiState<StatisticsUiData>> { UiState.Content(it) }
        .catch { emit(UiState.Error(UiText.DynamicString(it.message ?: "Помилка"))) }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    private val _dialogState = MutableStateFlow<StatisticsDialogState>(StatisticsDialogState.None)
    val dialogState: StateFlow<StatisticsDialogState> = _dialogState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun onOpenSetup(entry: MatrixEntryUiModel) {
        _dialogState.value = StatisticsDialogState.SetupMatrix(entry, entry.startWeight.toString(), entry.targetWeight.toString())
    }

    fun onConfirmSetup(exerciseId: Int, start: String, target: String) {
        viewModelScope.launch {
            try {
                matrixRepo.updateMatrixGoals(exerciseId, start.toFloatOrNull() ?: 0f, target.toFloatOrNull() ?: 0f)
                recalculateGlobalRank()
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка операції")))
            }
        }
    }

    private fun recalculateGlobalRank() {
        viewModelScope.launch {
            try {
                recalculateGlobalRankUseCase()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка оновлення рангу")))
            }
        }
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel) {
        if (!entry.isActive) return
        viewModelScope.launch {
            try {
                val date = viewingDateRepo.selectedDate.value
                val existingLog = getLogForDateUseCase(entry.exerciseId, date)
                
                _dialogState.value = StatisticsDialogState.LogWorkoutSets(
                    entry = entry,
                    existingLog = existingLog
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка завантаження логів")))
            }
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<WorkoutSetInput>, feedback: String) {
        viewModelScope.launch {
            try {
                val date = viewingDateRepo.selectedDate.value
                // UseCase handles timestamp calculation via AppClock internally
                saveExerciseSetsUseCase(exerciseId, sets, date, feedback)
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка збереження результатів")))
            }
        }
    }

    fun addSet() {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets) {
            _dialogState.value = current.copy(sets = (current.sets + WorkoutSetInput()).toImmutableList())
        }
    }

    fun removeSet() {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets && current.sets.size > 1) {
            _dialogState.value = current.copy(sets = current.sets.dropLast(1).toImmutableList())
        }
    }

    fun updateSetInput(setId: Long, weight: String, reps: String) {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets) {
            val newList = current.sets.map { 
                if (it.id == setId) it.copy(weight = weight, reps = reps) else it 
            }.toImmutableList()
            _dialogState.value = current.copy(sets = newList)
        }
    }

    fun onDismissDialog() { _dialogState.value = StatisticsDialogState.None }
}
