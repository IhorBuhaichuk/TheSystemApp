package com.ihor.thesystem.feature.mode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.usecase.AdvanceCycleDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ModeDialogState {
    object None                           : ModeDialogState()
    object ConfirmAdvance                 : ModeDialogState()
    data class EditSchedule(val day: Int) : ModeDialogState()
}

sealed class ModeEvent {
    object DayAdvanced : ModeEvent()
}

@HiltViewModel
class ModeViewModel @Inject constructor(
    private val playerRepo:      PlayerRepository,
    private val questRepo:       QuestRepository,
    private val scheduleRepo:    ScheduleRepository,
    private val advanceCycleDay: AdvanceCycleDayUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<ModeUiData>> = playerRepo.getPlayer()
        .filterNotNull()
        .flatMapLatest { player ->
            val d1Flow = scheduleRepo.getScheduleForDay(1)
            val d2Flow = scheduleRepo.getScheduleForDay(2)
            val d3Flow = scheduleRepo.getScheduleForDay(3)
            val d4Flow = scheduleRepo.getScheduleForDay(4)
            val dailyFlow = questRepo.getActiveDailyQuest()
            val mainFlow = questRepo.getActiveMainQuest()

            combine(d1Flow, d2Flow, d3Flow, d4Flow, dailyFlow, mainFlow) { array ->
                val d1 = array[0] as? ScheduleDay
                val d2 = array[1] as? ScheduleDay
                val d3 = array[2] as? ScheduleDay
                val d4 = array[3] as? ScheduleDay
                val daily = array[4] as? Quest
                val main = array[5] as? Quest

                val allDays = listOf(d1, d2, d3, d4)
                val current = allDays.getOrNull(player.currentCycleDay - 1)
                
                // Parsing recommendations from task name: "NAME | 70kg | 3x12"
                val exercises = main?.tasks?.map { task ->
                    val parts = task.name.split(" | ")
                    ExerciseWorkoutUiModel(
                        name = parts.getOrNull(0) ?: task.name,
                        recommendation = if (parts.size > 1) parts.drop(1).joinToString(" | ") else null
                    )
                } ?: emptyList()

                ModeUiData(
                    currentCycleDay = player.currentCycleDay,
                    isPenaltyActive = player.isPenaltyActive,
                    days = allDays.mapIndexedNotNull { i, day ->
                        day?.toCycleDayUiModel(
                            dayNum   = i + 1,
                            isActive = (i + 1) == player.currentCycleDay
                        )
                    },
                    activeDayData = current?.toActiveDayUiModel(exercises, daily)
                )
            }.map<ModeUiData, UiState<ModeUiData>> { UiState.Content(it) }
        }
        .catch { emit(UiState.Error(it.message ?: "Помилка")) }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    private val _dialogState = MutableStateFlow<ModeDialogState>(ModeDialogState.None)
    val dialogState: StateFlow<ModeDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<ModeEvent>()
    val events = _events.asSharedFlow()

    fun onNextDayTap()              { _dialogState.value = ModeDialogState.ConfirmAdvance }
    fun onEditScheduleTap(day: Int) { _dialogState.value = ModeDialogState.EditSchedule(day) }
    fun onDismissDialog()           { _dialogState.value = ModeDialogState.None }

    fun onConfirmAdvance() {
        viewModelScope.launch {
            advanceCycleDay()
            onDismissDialog()
            _events.emit(ModeEvent.DayAdvanced)
        }
    }

    fun onForceCompleteDay() {
        viewModelScope.launch {
            advanceCycleDay(forceComplete = true)
            onDismissDialog()
            _events.emit(ModeEvent.DayAdvanced)
        }
    }
}

private fun ScheduleDay.toCycleDayUiModel(dayNum: Int, isActive: Boolean) =
    CycleDayUiModel(
        dayNumber   = dayNum,
        label       = "ДЕНЬ $dayNum",
        type        = if (workoutTemplateId != null) DayType.WORKOUT else DayType.REST,
        isActive    = isActive,
        workoutName = workoutTemplateName
    )

private fun ScheduleDay.toActiveDayUiModel(
    exercises: List<ExerciseWorkoutUiModel>,
    dailyQuest: Quest?
): ActiveDayUiModel {
    val debuffLabels = mapOf(
        1 to "СЛАБКІСТЬ",
        2 to "ЦНС",
        3 to null,
        4 to null
    )
    return ActiveDayUiModel(
        dayNumber   = cycleDay,
        debuffName  = debuffLabels[cycleDay],
        dailyTasks  = if (dailyQuest != null) listOf(dailyQuest) else emptyList(),
        workoutName = workoutTemplateName,
        exercises   = exercises
    )
}
