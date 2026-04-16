package com.ihor.thesystem.feature.mode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.AdvanceCycleDayUseCase
import com.ihor.thesystem.domain.usecase.DayFinalizationResult
import com.ihor.thesystem.domain.usecase.FinalizeDayUseCase
import com.ihor.thesystem.domain.usecase.GenerateDailyQuestsUseCase
import com.ihor.thesystem.feature.mode.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val advanceCycleDay: AdvanceCycleDayUseCase,
    private val generateQuests:  GenerateDailyQuestsUseCase,
    private val finalizeDay:     FinalizeDayUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ModeUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ModeUiData>> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<ModeDialogState>(ModeDialogState.None)
    val dialogState: StateFlow<ModeDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<ModeEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var scheduleJob: Job? = null
    private var selectedDay: Int = 1

    init {
        viewModelScope.launch {
            try {
                generateQuests()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Помилка ініціалізації квестів")
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Не вдалося згенерувати квести на сьогодні"))
            }
            
            playerRepo.getPlayer().filterNotNull().collect { player ->
                val isFirstLoad = _uiState.value is UiState.Loading
                if (isFirstLoad) {
                    selectedDay = player.currentCycleDay
                }
                loadDataForDay(selectedDay, player.currentCycleDay, player.isPenaltyActive)
            }
        }
    }

    private fun loadDataForDay(day: Int, currentCycleDay: Int, isPenaltyActive: Boolean) {
        scheduleJob?.cancel()
        scheduleJob = viewModelScope.launch {
            val schedulesFlow = scheduleRepo.getSchedulesForDays(listOf(1, 2, 3, 4))
            val dailyFlow = questRepo.getActiveDailyQuest()
            val mainFlow = questRepo.getActiveMainQuest()

            combine(schedulesFlow, dailyFlow, mainFlow) { schedules, daily, main ->
                val allDays = (1..4).map { d -> schedules.find { it.cycleDay == d } }
                val currentSelected = allDays.getOrNull(day - 1)
                
                val exercises = if (day == currentCycleDay) {
                    main?.tasks?.map { task ->
                        val recStr = if (task.recommendedWeight != null) {
                            "${task.recommendedWeight}кг | ${task.recommendedSets}x${task.recommendedReps}"
                        } else null
                        ExerciseWorkoutUiModel(name = task.name, recommendation = recStr)
                    } ?: emptyList()
                } else {
                    currentSelected?.exercises?.map { ExerciseWorkoutUiModel(name = it.name) } ?: emptyList()
                }

                ModeUiData(
                    currentCycleDay = currentCycleDay,
                    selectedDay = day,
                    isPenaltyActive = isPenaltyActive,
                    days = allDays.mapIndexedNotNull { i, scheduleDay ->
                        scheduleDay?.toCycleDayUiModel(
                            dayNum = i + 1,
                            isActive = (i + 1) == currentCycleDay,
                            isSelected = (i + 1) == day
                        )
                    }.toImmutableList(),
                    activeDayData = currentSelected?.toActiveDayUiModel(
                        exercises.toImmutableList(), 
                        if(day == currentCycleDay) daily else null
                    )
                )
            }
            .catch { e ->
                Timber.e(e, "Помилка завантаження розкладу для дня $day")
                _uiEvents.emit(UiEvent.ShowError("Не вдалося завантажити дані розкладу: ${e.localizedMessage}"))
            }
            .collect { data ->
                _uiState.value = UiState.Content(data)
            }
        }
    }

    fun onCycleDayTap(day: Int) {
        selectedDay = day
        viewModelScope.launch {
            try {
                val player = playerRepo.getPlayer().firstOrNull() ?: return@launch
                loadDataForDay(day, player.currentCycleDay, player.isPenaltyActive)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Помилка перемикання на день $day")
                _uiEvents.emit(UiEvent.ShowError("Помилка перемикання дня: ${e.localizedMessage}"))
            }
        }
    }

    fun onNextDayTap()              { _dialogState.value = ModeDialogState.ConfirmAdvance }
    fun onEditScheduleTap(day: Int) { _dialogState.value = ModeDialogState.EditSchedule(day) }
    fun onCycleDayLongPress(day: Int) { _dialogState.value = ModeDialogState.SyncAnchor(day) }
    fun onDismissDialog()           { _dialogState.value = ModeDialogState.None }

    fun onConfirmAdvance() {
        viewModelScope.launch {
            try {
                advanceCycleDay()
                val result = finalizeDay()
                onDismissDialog()
                
                when (result) {
                    is DayFinalizationResult.LevelUp ->
                        _events.emit(ModeEvent.LevelUp)
                    is DayFinalizationResult.PenaltyZoneEntered ->
                        _events.emit(ModeEvent.PenaltyActivated)
                    else -> _events.emit(ModeEvent.DayAdvanced)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Помилка завершення дня")
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Не вдалося завершити день. Перевірте з'єднання або дані."))
            }
        }
    }

    fun onForceCompleteDay() {
        viewModelScope.launch {
            try {
                advanceCycleDay(forceComplete = true)
                val result = finalizeDay()
                onDismissDialog()
                
                when (result) {
                    is DayFinalizationResult.LevelUp ->
                        _events.emit(ModeEvent.LevelUp)
                    is DayFinalizationResult.PenaltyZoneEntered ->
                        _events.emit(ModeEvent.PenaltyActivated)
                    else -> _events.emit(ModeEvent.DayAdvanced)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Помилка примусового завершення дня")
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка примусового завершення"))
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
                Timber.e(e, "Помилка синхронізації циклу на день $day")
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка синхронізації циклу"))
            }
        }
    }
}

private fun ScheduleDay.toCycleDayUiModel(dayNum: Int, isActive: Boolean, isSelected: Boolean) =
    CycleDayUiModel(
        dayNumber   = dayNum,
        type        = if (workoutTemplateId != null) DayType.WORKOUT else DayType.REST,
        isActive    = isActive,
        isSelected  = isSelected,
        workoutName = workoutTemplateName
    )

private fun ScheduleDay.toActiveDayUiModel(
    exercises: ImmutableList<ExerciseWorkoutUiModel>,
    dailyQuest: Quest?
): ActiveDayUiModel {
    return ActiveDayUiModel(
        dayNumber   = cycleDay,
        debuffName  = if (cycleDay == 1) "СЛАБКІСТЬ" else if (cycleDay == 2) "ЦНС" else null,
        dailyTasks  = (if (dailyQuest != null) listOf(dailyQuest) else emptyList<Quest>()).toImmutableList(),
        workoutName = workoutTemplateName,
        exercises   = exercises
    )
}
