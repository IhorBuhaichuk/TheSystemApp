package com.ihor.thesystem.feature.mode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
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
    private val scheduleRepo:    ScheduleRepository,
    private val advanceCycleDay: AdvanceCycleDayUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<ModeUiData>> = playerRepo.getPlayer()
        .filterNotNull()
        .flatMapLatest { player ->
            combine(
                scheduleRepo.getScheduleForDay(1),
                scheduleRepo.getScheduleForDay(2),
                scheduleRepo.getScheduleForDay(3),
                scheduleRepo.getScheduleForDay(4)
            ) { d1, d2, d3, d4 ->
                val allDays = listOf(d1, d2, d3, d4)
                val current = allDays.getOrNull(player.currentCycleDay - 1)
                ModeUiData(
                    currentCycleDay = player.currentCycleDay,
                    isPenaltyActive = player.isPenaltyActive,
                    days = allDays.mapIndexedNotNull { i, day ->
                        day?.toCycleDayUiModel(
                            dayNum   = i + 1,
                            isActive = (i + 1) == player.currentCycleDay
                        )
                    },
                    activeDayData = current?.toActiveDayUiModel()
                )
            }
        }
        .map<ModeUiData, UiState<ModeUiData>> { UiState.Content(it) }
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

// ── Mappers ───────────────────────────────────────────────────────────────────
private fun ScheduleDay.toCycleDayUiModel(dayNum: Int, isActive: Boolean) =
    CycleDayUiModel(
        dayNumber   = dayNum,
        label       = "ДЕНЬ $dayNum",
        type        = if (workoutTemplateId != null) DayType.WORKOUT else DayType.REST,
        isActive    = isActive,
        workoutName = workoutTemplateName
    )

private fun ScheduleDay.toActiveDayUiModel(): ActiveDayUiModel {
    val debuffLabels = mapOf(
        1 to "СЛАБКІСТЬ",
        2 to "ЦНС",
        3 to null,
        4 to null
    )
    return ActiveDayUiModel(
        dayNumber   = cycleDay,
        debuffName  = debuffLabels[cycleDay],
        dailyTasks  = dailyTaskNames.map { ActiveTaskUiModel(name = it) },
        workoutName = workoutTemplateName,
        exercises   = exerciseNames
    )
}