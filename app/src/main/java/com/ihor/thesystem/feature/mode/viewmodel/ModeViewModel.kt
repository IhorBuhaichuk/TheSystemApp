package com.ihor.thesystem.feature.mode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.AdvanceCycleDayUseCase
import com.ihor.thesystem.domain.usecase.GenerateDailyQuestsUseCase
import com.ihor.thesystem.feature.mode.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
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
}

@HiltViewModel
class ModeViewModel @Inject constructor(
    private val playerRepo:      PlayerRepository,
    private val questRepo:       QuestRepository,
    private val scheduleRepo:    ScheduleRepository,
    private val configRepo:      SystemConfigRepository,
    private val advanceCycleDay: AdvanceCycleDayUseCase,
    private val generateQuests:  GenerateDailyQuestsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ModeUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ModeUiData>> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<ModeDialogState>(ModeDialogState.None)
    val dialogState: StateFlow<ModeDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<ModeEvent>()
    val events = _events.asSharedFlow()

    private var scheduleJob: Job? = null
    private var selectedDay: Int = 1

    init {
        viewModelScope.launch {
            generateQuests()
            
            playerRepo.getPlayer().filterNotNull().collect { player ->
                // При першому завантаженні або зміні поточного дня циклу оновлюємо вибраний день
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
            val d1Flow = scheduleRepo.getScheduleForDay(1)
            val d2Flow = scheduleRepo.getScheduleForDay(2)
            val d3Flow = scheduleRepo.getScheduleForDay(3)
            val d4Flow = scheduleRepo.getScheduleForDay(4)
            val dailyFlow = questRepo.getActiveDailyQuest()
            val mainFlow = questRepo.getActiveMainQuest()

            combine(listOf(d1Flow, d2Flow, d3Flow, d4Flow, dailyFlow, mainFlow)) { array ->
                val d1 = array[0] as? ScheduleDay
                val d2 = array[1] as? ScheduleDay
                val d3 = array[2] as? ScheduleDay
                val d4 = array[3] as? ScheduleDay
                val daily = array[4] as? Quest
                val main = array[5] as? Quest

                val allDays = listOf(d1, d2, d3, d4)
                val currentSelected = allDays.getOrNull(day - 1)
                
                // Вправи показуємо:
                // 1. Якщо це поточний день циклу - з активного Main квесту (там актуальні ваги)
                // 2. Якщо інший день - просто назви з шаблону розкладу
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
            }.collect { data ->
                _uiState.value = UiState.Content(data)
            }
        }
    }

    fun onCycleDayTap(day: Int) {
        selectedDay = day
        viewModelScope.launch {
            val player = playerRepo.getPlayer().firstOrNull() ?: return@launch
            loadDataForDay(day, player.currentCycleDay, player.isPenaltyActive)
        }
    }

    fun onNextDayTap()              { _dialogState.value = ModeDialogState.ConfirmAdvance }
    fun onEditScheduleTap(day: Int) { _dialogState.value = ModeDialogState.EditSchedule(day) }
    fun onCycleDayLongPress(day: Int) { _dialogState.value = ModeDialogState.SyncAnchor(day) }
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

    fun onConfirmSync(day: Int) {
        viewModelScope.launch {
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
        }
    }
}

private fun ScheduleDay.toCycleDayUiModel(dayNum: Int, isActive: Boolean, isSelected: Boolean) =
    CycleDayUiModel(
        dayNumber   = dayNum,
        label       = when(dayNum) {
            1 -> "ДЕНЬ"
            2 -> "НІЧ"
            3 -> "ВІДСИПНИЙ"
            4 -> "ВИХІДНИЙ"
            else -> "ДЕНЬ $dayNum"
        },
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
