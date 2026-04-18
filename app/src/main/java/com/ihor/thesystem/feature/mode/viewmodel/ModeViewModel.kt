package com.ihor.thesystem.feature.mode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.util.*
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.*
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class ModeDialogState {
    object None                           : ModeDialogState()
    object ConfirmAdvance                 : ModeDialogState()
    data class EditSchedule(val day: Int) : ModeDialogState()
    data class SyncAnchor(val day: Int)   : ModeDialogState()
}

sealed class ModeEvent {
    object DayAdvanced : ModeEvent()
    object CycleSynced : ModeEvent()
    object LevelUp : ModeEvent()
    object PenaltyActivated : ModeEvent()
}

@HiltViewModel
class ModeViewModel @Inject constructor(
    private val playerRepo:      PlayerRepository,
    private val questRepo:       QuestRepository,
    private val scheduleRepo:    ScheduleRepository,
    private val configRepo:      SystemConfigRepository,
    private val debuffRepo:      DebuffRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val getLogForDateUseCase: GetLogForDateUseCase,
    private val saveExerciseSetsUseCase: SaveExerciseSetsUseCase,
    private val generateQuests:  GenerateDailyQuestsUseCase,
    private val finalizeDayTransaction: FinalizeDayTransactionUseCase,
    private val getStatisticsDataUseCase: GetStatisticsDataUseCase,
    private val dispatchers:     DispatcherProvider,
    private val logger:          AppLogger
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _exerciseInputs = MutableStateFlow<Map<Int, ImmutableList<WorkoutSetInput>>>(emptyMap())

    private val _dialogState = MutableStateFlow<ModeDialogState>(ModeDialogState.None)
    val dialogState: StateFlow<ModeDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<ModeEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<ModeUiData>> = combine(_selectedDay, _exerciseInputs) { selDay, inputs -> 
        selDay to inputs 
    }.flatMapLatest { (selDay, inputs) ->
        val playerFlow = playerRepo.getPlayer().filterNotNull()
        val schedulesFlow = scheduleRepo.getSchedulesForDays(CycleConfig.MICROCYCLE_DAYS)
        val dailyQuestFlow = questRepo.getActiveDailyQuest()
        val mainQuestFlow = questRepo.getActiveMainQuest()
        val debuffsFlow = debuffRepo.getDebuffsForCycleDay(selDay)
        val statsFlow = getStatisticsDataUseCase()

        combine(
            listOf(
                playerFlow,
                schedulesFlow,
                dailyQuestFlow,
                mainQuestFlow,
                debuffsFlow,
                statsFlow
            )
        ) { args ->
            val player = args[0] as Player
            val schedules = args[1] as List<ScheduleDay>
            val daily = args[2] as Quest?
            val main = args[3] as Quest?
            val debuffs = args[4] as List<DebuffConfig>
            val stats = args[5] as StatisticsUiData
            val allDays = CycleConfig.MICROCYCLE_DAYS.map { d -> schedules.find { it.cycleDay == d } }
            val currentSelectedSchedule = allDays.getOrNull(selDay - 1)

            val exercises = if (selDay == player.currentCycleDay) {
                main?.tasks?.mapNotNull { task ->
                    val exerciseId = task.exerciseId ?: return@mapNotNull null
                    val recStr = if (task.recommendedWeight != null) {
                        "${task.recommendedWeight}кг | ${task.recommendedSets}x${task.recommendedReps}"
                    } else null
                    
                    val currentInputs = inputs[exerciseId] ?: persistentListOf(
                        WorkoutSetInput(), WorkoutSetInput(), WorkoutSetInput()
                    )

                    ExerciseWorkoutUiModel(
                        exerciseId = exerciseId,
                        name = task.name, 
                        recommendation = recStr,
                        sets = currentInputs
                    )
                } ?: emptyList()
            } else {
                currentSelectedSchedule?.exercises?.map { 
                    ExerciseWorkoutUiModel(exerciseId = it.id, name = it.name)
                } ?: emptyList()
            }

            val data = ModeUiData(
                currentCycleDay = player.currentCycleDay,
                selectedDay = selDay,
                isPenaltyActive = player.isPenaltyActive,
                days = allDays.mapIndexedNotNull { i, scheduleDay ->
                    scheduleDay?.toCycleDayUiModel(
                        dayNum = i + 1,
                        isActive = (i + 1) == player.currentCycleDay,
                        isSelected = (i + 1) == selDay
                    )
                }.toImmutableList(),
                activeDayData = currentSelectedSchedule?.toActiveDayUiModel(
                    exercises = exercises.toImmutableList(), 
                    dailyQuest = if(selDay == player.currentCycleDay) daily else null,
                    debuffs = debuffs,
                    matrixEntries = stats.matrixEntries
                )
            )
            UiState.Content(data) as UiState<ModeUiData>
        }
    }
    .catch { e ->
        logger.e(e, "Помилка реактивного потоку ModeViewModel")
        emit(UiState.Error(UiText.DynamicString("Помилка завантаження даних: ${e.localizedMessage}")))
    }
    .flowOn(dispatchers.io)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    init {
        viewModelScope.launch(dispatchers.io) {
            try {
                generateQuests()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logger.e(e, "Помилка ініціалізації квестів")
            }
        }

        viewModelScope.launch(dispatchers.mainImmediate) {
            playerRepo.getPlayer().filterNotNull().firstOrNull()?.let {
                _selectedDay.value = it.currentCycleDay
            }
        }
    }

    fun onCycleDayTap(day: Int) {
        _selectedDay.value = day
    }

    fun onNextDayTap()              { _dialogState.value = ModeDialogState.ConfirmAdvance }
    fun onEditScheduleTap(day: Int) { _dialogState.value = ModeDialogState.EditSchedule(day) }
    fun onCycleDayLongPress(day: Int) { _dialogState.value = ModeDialogState.SyncAnchor(day) }
    fun onDismissDialog()           { _dialogState.value = ModeDialogState.None }

    fun onConfirmAdvance() {
        viewModelScope.launch(dispatchers.mainImmediate) {
            advanceAndFinalize(forceComplete = false)
        }
    }

    fun onForceCompleteDay() {
        viewModelScope.launch(dispatchers.mainImmediate) {
            advanceAndFinalize(forceComplete = true)
        }
    }

    fun onUpdateSetInput(exerciseId: Int, setId: Long, weight: String, reps: String) {
        val currentInputs = _exerciseInputs.value[exerciseId] ?: persistentListOf(
            WorkoutSetInput(), WorkoutSetInput(), WorkoutSetInput()
        )
        val newList = currentInputs.map {
            if (it.id == setId) it.copy(weight = weight, reps = reps) else it
        }.toImmutableList()
        _exerciseInputs.value = _exerciseInputs.value + (exerciseId to newList)
    }

    private suspend fun advanceAndFinalize(forceComplete: Boolean) {
        val currentState = uiState.value
        if (currentState is UiState.Content) {
            val date = viewingDateRepo.selectedDate.value
            currentState.data.activeDayData?.exercises?.forEach { ex ->
                val sets = ex.sets
                if (sets.any { it.weight.isNotEmpty() && it.reps.isNotEmpty() }) {
                    saveExerciseSetsUseCase(ex.exerciseId, sets, date, null)
                }
            }
        }

        val result = safeCall {
            finalizeDayTransaction(forceComplete = forceComplete)
        }
        
        onDismissDialog()
        
        when (result) {
            is Result.Success -> {
                when (result.data) {
                    is DayFinalizationResult.LevelUp ->
                        _events.emit(ModeEvent.LevelUp)
                    is DayFinalizationResult.PenaltyZoneEntered ->
                        _events.emit(ModeEvent.PenaltyActivated)
                    else -> _events.emit(ModeEvent.DayAdvanced)
                }
            }
            is Result.Error -> {
                logger.e(null, "Помилка фіналізації: ${result.error}")
                _uiEvents.emit(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    fun onConfirmSync(day: Int) {
        viewModelScope.launch(dispatchers.mainImmediate) {
            val result = safeCall {
                playerRepo.updateCurrentCycleDay(day)
                
                val currentConfig = configRepo.getConfigFlow().firstOrNull()
                if (currentConfig != null) {
                    configRepo.updateConfig(
                        currentConfig.copy(
                            cycleAnchorDateTimestamp = LocalDate.now().toEpochDay(),
                            cycleAnchorDay = day
                        )
                    )
                } else {
                    Result.Error(DataError.Local.NOT_FOUND)
                }
                
                Result.Success(Unit)
            }

            onDismissDialog()

            when (result) {
                is Result.Success -> _events.emit(ModeEvent.CycleSynced)
                is Result.Error -> {
                    logger.e(null, "Помилка синхронізації циклу: ${result.error}")
                    _uiEvents.emit(UiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }
}

private fun ScheduleDay.toCycleDayUiModel(dayNum: Int, isActive: Boolean, isSelected: Boolean) =
    CycleDayUiModel(
        dayNumber   = dayNum,
        type        = if (isWorkoutDay) DayType.WORKOUT else DayType.REST,
        isActive    = isActive,
        isSelected  = isSelected,
        workoutName = workoutTemplateName
    )

private fun ScheduleDay.toActiveDayUiModel(
    exercises: ImmutableList<ExerciseWorkoutUiModel>,
    dailyQuest: Quest?,
    debuffs: List<DebuffConfig>,
    matrixEntries: ImmutableList<com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel>
): ActiveDayUiModel {
    return ActiveDayUiModel(
        dayNumber   = cycleDay,
        debuffName  = debuffs.firstOrNull()?.condition,
        dailyTasks  = (if (dailyQuest != null) listOf(dailyQuest) else emptyList<Quest>()).toImmutableList(),
        workoutName = workoutTemplateName,
        exercises   = exercises,
        matrixEntries = matrixEntries.filter { entry -> 
            exercises.any { it.name.equals(entry.exerciseName, ignoreCase = true) } 
        }.toImmutableList()
    )
}
