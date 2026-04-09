package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistoryWithId
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.entity.ReferenceMatrixEntity
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import com.ihor.thesystem.domain.usecase.SaveExerciseSetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
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
    private val weightLogDao: WeightLogDao,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase,
    private val saveExerciseSetsUseCase: SaveExerciseSetsUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<StatisticsUiData>> = combine(
        playerRepo.getPlayer().filterNotNull(),
        matrixRepo.getAllEntries(),
        matrixRepo.getAllReferences(),
        analyticsRepo.getAllWeightHistories(),
        viewingDateRepo.selectedDate,
        configRepo.getConfigFlow().filterNotNull(),
        weightLogDao.getAllLogs()
    ) { args: Array<Any?> ->
        val player = args[0] as Player
        val matrix = args[1] as List<ProgressionMatrixEntry>
        val references = args[2] as List<ReferenceMatrixEntity>
        val allHistories = args[3] as List<ExerciseWeightHistoryWithId>
        val selectedDate = args[4] as LocalDate
        val config = args[5] as SystemConfig
        val weightHistory = args[6] as List<WeightLogEntity>

        val cycleDay = calculateCycleDay(
            targetDate = selectedDate,
            anchorEpochDay = config.cycleAnchorDateTimestamp,
            anchorCycleDay = config.cycleAnchorDay
        )

        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()
        val activeExerciseIds = schedule?.exercises?.map { it.id } ?: emptyList()

        // Групуємо історії по exerciseId в пам'яті
        val historiesMap = allHistories.groupBy { it.exerciseId }

        val updatedEntries = matrix.map { entry ->
            val ref = references.find { it.exerciseName.equals(entry.exerciseName, ignoreCase = true) }
            val isExerciseActive = activeExerciseIds.contains(entry.exerciseId)
            val orderIndex = if (isExerciseActive) activeExerciseIds.indexOf(entry.exerciseId) else 999

            val m0 = ref?.milestones?.get("M0")?.toFloat() ?: entry.startWeight
            val m12 = ref?.milestones?.get("M12")?.toFloat() ?: entry.targetWeight
            
            // Беремо історію з мапи
            val history = historiesMap[entry.exerciseId]?.map { 
                ExerciseWeightHistory(it.weight, it.timestamp) 
            } ?: emptyList()

            entry.toUiModel(isExerciseActive, orderIndex, history).copy(
                startWeight = m0,
                targetWeight = m12
            )
        }.sortedWith(compareBy({ !it.isActive }, { it.orderIndex }, { it.exerciseName }))

        StatisticsUiData(
            playerName      = player.name,
            playerClass     = player.playerClass,
            currentMonth    = player.currentMonth,
            totalMonths     = 12,
            currentWeek     = player.currentWeek,
            currentCycleDay = cycleDay,
            isPenaltyActive = player.isPenaltyActive,
            globalRank      = player.globalRank,
            matrixEntries   = updatedEntries.toImmutableList(),
            weightHistory   = weightHistory.sortedBy { it.timestamp }.toImmutableList()
        )
    }
    .map<StatisticsUiData, UiState<StatisticsUiData>> { UiState.Content(it) }
    .catch { emit(UiState.Error(it.message ?: "Помилка")) }
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
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    private fun recalculateGlobalRank() {
        viewModelScope.launch {
            try {
                matrixRepo.recalculateGlobalRank()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка оновлення рангу"))
            }
        }
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel) {
        if (!entry.isActive) return
        viewModelScope.launch {
            try {
                val date = viewingDateRepo.selectedDate.value
                val zoneId = ZoneId.systemDefault()
                val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                
                val existingLog = analyticsRepo.getLogForExerciseOnDate(entry.exerciseId, startOfDay, endOfDay)
                
                _dialogState.value = StatisticsDialogState.LogWorkoutSets(
                    entry = entry,
                    existingLog = existingLog
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка завантаження логів"))
            }
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<WorkoutSetInput>, feedback: String) {
        viewModelScope.launch {
            try {
                val date = viewingDateRepo.selectedDate.value
                val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                // Використовуємо UseCase для збереження та автоматичного перерахунку рангу
                saveExerciseSetsUseCase(exerciseId, sets, timestamp, feedback)
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка збереження результатів"))
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
        weightHistory    = history.toImmutableList(),
        nextRecommendedWeight = nextRecommendedWeight,
        nextRecommendedSets = nextRecommendedSets,
        nextRecommendedReps = nextRecommendedReps,
        lastAiFeedback = lastAiFeedback
    )
}
