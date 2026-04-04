package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<StatisticsUiData>> = combine(
        playerRepo.getPlayer().filterNotNull(),
        viewingDateRepo.selectedDate,
        configRepo.getConfigFlow().filterNotNull(),
        matrixRepo.getAllReferences()
    ) { player, selectedDate, config, references ->
        Quad(player, selectedDate, config, references)
    }.flatMapLatest { (player, selectedDate, config, references) ->
        matrixRepo.getAllEntries().flatMapLatest { matrix ->
            val historiesFlows = matrix.map { entry ->
                analyticsRepo.getWeightHistory(entry.exerciseId).map { history ->
                    entry.exerciseId to history
                }
            }
            
            if (historiesFlows.isEmpty()) {
                flowOf(emptyMap<Int, List<ExerciseWeightHistory>>())
            } else {
                combine(historiesFlows) { it.toMap() }
            }.map { historiesMap ->
                val cycleDay = calculateCycleDay(
                    targetDate = selectedDate,
                    anchorEpochDay = config.cycleAnchorDateTimestamp,
                    anchorCycleDay = config.cycleAnchorDay
                )

                val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()
                val activeExerciseIds = schedule?.exercises?.map { it.id } ?: emptyList()

                val updatedEntries = matrix.map { entry ->
                    val ref = references.find { it.exerciseName.equals(entry.exerciseName, ignoreCase = true) }
                    val isActive = activeExerciseIds.contains(entry.exerciseId)
                    val orderIndex = if (isActive) activeExerciseIds.indexOf(entry.exerciseId) else 999

                    val m0 = ref?.milestones?.get("M0")?.toFloat() ?: entry.startWeight
                    val m12 = ref?.milestones?.get("M12")?.toFloat() ?: entry.targetWeight
                    
                    entry.toUiModel(isActive, orderIndex, historiesMap[entry.exerciseId] ?: emptyList()).copy(
                        startWeight = m0,
                        targetWeight = m12
                    )
                }.sortedWith(compareBy({ !it.isActive }, { it.orderIndex }, { it.exerciseName }))

                StatisticsUiData(
                    playerName      = player.name,
                    playerClass     = player.playerClass,
                    currentMonth    = player.currentMonth,
                    currentWeek     = player.currentWeek,
                    currentCycleDay = cycleDay,
                    isPenaltyActive = player.isPenaltyActive,
                    globalRank      = player.globalRank,
                    matrixEntries   = updatedEntries.toImmutableList(),
                    tonnageStats    = emptyList<DailyTonnageStats>().toImmutableList() // Placeholder for actual tonnage stats
                )
            }
        }
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
        if (!entry.isActive) return
        _dialogState.value = StatisticsDialogState.LogWorkoutSets(
            entry = entry,
            sets = List(3) { WorkoutSetInput() }.toImmutableList()
        )
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<WorkoutSetInput>) {
        viewModelScope.launch {
            val date = viewingDateRepo.selectedDate.value
            val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            matrixRepo.saveExerciseSetsWithDate(exerciseId, sets, timestamp)
            onDismissDialog()
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

    fun onDismissDialog() {
        _dialogState.value = StatisticsDialogState.None
    }

    private fun ProgressionMatrixEntry.toUiModel(isActive: Boolean, orderIndex: Int, history: List<ExerciseWeightHistory>) = MatrixEntryUiModel(
        exerciseId       = exerciseId,
        exerciseName     = exerciseName,
        startWeight      = startWeight,
        targetWeight     = targetWeight,
        currentWeight    = currentWeight,
        targetWeightNote = targetWeightNote,
        weeklyStep       = weeklyStep,
        progressPercent  = progressPercent,
        currentRank      = currentRank,
        completedCycles  = completedCycles,
        isActive         = isActive,
        orderIndex       = orderIndex,
        weightHistory    = history.toImmutableList()
    )
}

data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
