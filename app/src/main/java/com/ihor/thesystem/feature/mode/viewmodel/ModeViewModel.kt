package com.ihor.thesystem.feature.mode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.util.AppLogger
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.core.util.safeCall
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.*
import com.ihor.thesystem.feature.mode.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
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
    private val generateQuests:  GenerateDailyQuestsUseCase,
    private val finalizeDayTransaction: FinalizeDayTransactionUseCase,
    private val logger:          AppLogger
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _dialogState = MutableStateFlow<ModeDialogState>(ModeDialogState.None)
    val dialogState: StateFlow<ModeDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<ModeEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<ModeUiData>> = _selectedDay.flatMapLatest { selDay ->
        combine(
            playerRepo.getPlayer().filterNotNull(),
            scheduleRepo.getSchedulesForDays(CycleConfig.MICROCYCLE_DAYS),
            questRepo.getActiveDailyQuest(),
            questRepo.getActiveMainQuest(),
            debuffRepo.getDebuffsForCycleDay(selDay)
        ) { player, schedules, daily, main, debuffs ->
            val allDays = CycleConfig.MICROCYCLE_DAYS.map { d -> schedules.find { it.cycleDay == d } }
            val currentSelectedSchedule = allDays.getOrNull(selDay - 1)

            val exercises = if (selDay == player.currentCycleDay) {
                main?.tasks?.map { task ->
                    val recStr = if (task.recommendedWeight != null) {
                        "${task.recommendedWeight}кг | ${task.recommendedSets}x${task.recommendedReps}"
                    } else null
                    ExerciseWorkoutUiModel(name = task.name, recommendation = recStr)
                } ?: emptyList()
            } else {
                currentSelectedSchedule?.exercises?.map { ExerciseWorkoutUiModel(name = it.name) } ?: emptyList()
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
                    exercises.toImmutableList(), 
                    if(selDay == player.currentCycleDay) daily else null,
                    debuffs
                )
            )
            UiState.Content(data) as UiState<ModeUiData>
        }
    }
    .catch { e ->
        logger.e(e, "Помилка реактивного потоку ModeViewModel")
        emit(UiState.Error(UiText.DynamicString("Помилка завантаження даних: ${e.localizedMessage}")))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    init {
        viewModelScope.launch {
            try {
                generateQuests()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logger.e(e, "Помилка ініціалізації квестів")
            }
        }

        viewModelScope.launch {
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
        viewModelScope.launch {
            advanceAndFinalize(forceComplete = false)
        }
    }

    fun onForceCompleteDay() {
        viewModelScope.launch {
            advanceAndFinalize(forceComplete = true)
        }
    }

    private suspend fun advanceAndFinalize(forceComplete: Boolean) {
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
        viewModelScope.launch {
            try {
                playerRepo.updateCurrentCycleDay(day)
                
                val currentConfig = configRepo.getConfigFlow().firstOrNull()
                if (currentConfig != null) {
                    configRepo.updateConfig(
                        currentConfig.copy(
                            cycleAnchorDateTimestamp = LocalDate.now().toEpochDay(),
                            cycleAnchorDay = day
                        )
                    )
                }

                onDismissDialog()
                _events.emit(ModeEvent.CycleSynced)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logger.e(e, "Помилка синхронізації циклу")
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString("Помилка синхронізації циклу")))
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
    debuffs: List<DebuffConfig>
): ActiveDayUiModel {
    return ActiveDayUiModel(
        dayNumber   = cycleDay,
        debuffName  = debuffs.firstOrNull()?.condition,
        dailyTasks  = (if (dailyQuest != null) listOf(dailyQuest) else emptyList<Quest>()).toImmutableList(),
        workoutName = workoutTemplateName,
        exercises   = exercises
    )
}
