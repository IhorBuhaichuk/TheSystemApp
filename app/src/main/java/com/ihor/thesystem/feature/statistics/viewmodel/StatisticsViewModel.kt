package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.GetStatisticsDataUseCase
import com.ihor.thesystem.domain.usecase.LogWeightUseCase
import com.ihor.thesystem.domain.usecase.RecalculateGlobalRankUseCase
import com.ihor.thesystem.domain.usecase.UpdatePlayerHeightUseCase
import com.ihor.thesystem.feature.status.viewmodel.WorkoutSetInput
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
    private val recalculateGlobalRankUseCase: RecalculateGlobalRankUseCase,
    private val logWeightUseCase: LogWeightUseCase,
    private val updatePlayerHeightUseCase: UpdatePlayerHeightUseCase
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

    private val _currentSetInputs = MutableStateFlow<List<WorkoutSetInput>>(emptyList())

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun onOpenSetup(entry: MatrixEntryUiModel) {
        _dialogState.value = StatisticsDialogState.SetupMatrix(entry, entry.startWeight.toString(), entry.targetWeight.toString())
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel) {
        val initialSets = listOf(WorkoutSetInput())
        _currentSetInputs.value = initialSets
        _dialogState.value = StatisticsDialogState.LogWorkoutSets(entry, initialSets)
    }

    fun updateSetInput(id: Long, weight: String, reps: String) {
        _currentSetInputs.update { list ->
            list.map { if (it.id == id) it.copy(weight = weight, reps = reps) else it }
        }
        updateLogDialogState()
    }

    fun addSet() {
        _currentSetInputs.update { it + WorkoutSetInput() }
        updateLogDialogState()
    }

    fun removeSet() {
        _currentSetInputs.update { if (it.size > 1) it.dropLast(1) else it }
        updateLogDialogState()
    }

    private fun updateLogDialogState() {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets) {
            _dialogState.value = current.copy(sets = _currentSetInputs.value)
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<WorkoutSetInput>, feedback: String) {
        viewModelScope.launch {
            try {
                matrixRepo.saveExerciseSetsWithDate(
                    exerciseId = exerciseId,
                    sets = sets,
                    timestamp = System.currentTimeMillis(),
                    userFeedback = feedback
                )
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка збереження")))
            }
        }
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


    fun onOpenLogWeight() {
        _dialogState.value = StatisticsDialogState.LogWeight
    }

    fun onOpenEditHeight() {
        _dialogState.value = StatisticsDialogState.EditHeight
    }

    fun onWeightConfirmed(weight: Float) {
        viewModelScope.launch {
            logWeightUseCase(weight).onSuccess {
                onDismissDialog()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(it.message ?: "Помилка оновлення ваги")))
            }
        }
    }

    fun onHeightConfirmed(height: Float) {
        viewModelScope.launch {
            updatePlayerHeightUseCase(height).onSuccess {
                onDismissDialog()
            }.onFailure {
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(it.message ?: "Помилка оновлення зросту")))
            }
        }
    }

    fun onDismissDialog() { _dialogState.value = StatisticsDialogState.None }
}
