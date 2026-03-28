package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository
) : ViewModel() {

    val uiState: StateFlow<UiState<StatisticsUiData>> = combine(
        playerRepo.getPlayer().filterNotNull(),
        matrixRepo.getAllEntries(),
        matrixRepo.getAllReferences()
    ) { player, matrix, references ->
        val updatedEntries = matrix.map { entry ->
            val ref = references.find { it.exerciseName.equals(entry.exerciseName, ignoreCase = true) }
            if (ref != null) {
                val m0 = ref.milestones["M0"]?.toFloat() ?: entry.startWeight
                val m12 = ref.milestones["M12"]?.toFloat() ?: entry.targetWeight
                entry.toUiModel().copy(
                    startWeight = m0,
                    targetWeight = m12
                )
            } else {
                entry.toUiModel()
            }
        }

        StatisticsUiData(
            playerName      = player.name,
            playerClass     = player.playerClass,
            currentMonth    = player.currentMonth,
            currentWeek     = player.currentWeek,
            currentCycleDay = player.currentCycleDay,
            isPenaltyActive = player.isPenaltyActive,
            matrixEntries   = updatedEntries
        )
    }
    .map<StatisticsUiData, UiState<StatisticsUiData>> { UiState.Content(it) }
    .catch { emit(UiState.Error(it.message ?: "Помилка завантаження статистики")) }
    .stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    private val _dialogState = MutableStateFlow<StatisticsDialogState>(StatisticsDialogState.None)
    val dialogState: StateFlow<StatisticsDialogState> = _dialogState.asStateFlow()

    fun onOpenSetup(entry: MatrixEntryUiModel) {
        _dialogState.value = StatisticsDialogState.SetupMatrix(
            entry = entry,
            startWeight = entry.startWeight.toString(),
            targetWeight = entry.targetWeight.toString()
        )
    }

    fun onConfirmSetup(exerciseId: Int, start: String, target: String) {
        viewModelScope.launch {
            matrixRepo.updateMatrixGoals(
                exerciseId = exerciseId,
                startWeight = start.toFloatOrNull() ?: 0f,
                targetWeight = target.toFloatOrNull() ?: 0f
            )
            onDismissDialog()
        }
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel) {
        _dialogState.value = StatisticsDialogState.LogWorkoutSets(
            entry = entry,
            sets = List(3) { WorkoutSetInput() }
        )
    }

    fun addSet() {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets) {
            _dialogState.value = current.copy(sets = current.sets + WorkoutSetInput())
        }
    }

    fun removeSet() {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets && current.sets.size > 1) {
            _dialogState.value = current.copy(sets = current.sets.dropLast(1))
        }
    }

    fun updateSetInput(setId: Long, weight: String, reps: String) {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets) {
            val newList = current.sets.map { 
                if (it.id == setId) it.copy(weight = weight, reps = reps) else it 
            }
            _dialogState.value = current.copy(sets = newList)
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<WorkoutSetInput>) {
        viewModelScope.launch {
            matrixRepo.saveExerciseSets(exerciseId, sets)
            onDismissDialog()
        }
    }

    fun onDismissDialog() {
        _dialogState.value = StatisticsDialogState.None
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
}
