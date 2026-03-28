package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarDayUiModel(
    val date: LocalDate,
    val cycleDay: Int, // 1..4
    val label: String, // "Денна зміна", "Нічна зміна", "Відсипний", "Вихідний"
    val isToday: Boolean
)

data class CalendarUiState(
    val days: List<CalendarDayUiModel> = emptyList(),
    val todayInfo: CalendarDayUiModel? = null,
    val selectedDate: LocalDate? = null,
    val workoutResults: List<WorkoutResultUiModel> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = false
)

data class WorkoutResultUiModel(
    val exerciseName: String,
    val sets: List<SetResultUiModel>
)

data class SetResultUiModel(
    val weight: Double,
    val reps: Int
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val workoutDao: WorkoutDao,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _workoutResults = MutableStateFlow<List<WorkoutResultUiModel>>(emptyList())

    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        configRepo.getConfig().filterNotNull(),
        _selectedDate,
        _workoutResults
    ) { month, config, selectedDate, results ->
        val daysInMonth = month.lengthOfMonth()
        val calendarDays = (1..daysInMonth).map { day ->
            val date = month.atDay(day)
            val cycleDay = calculateCycleDay(
                targetDate = date,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
                anchorCycleDay = config.cycleAnchorDay
            )
            CalendarDayUiModel(
                date = date,
                cycleDay = cycleDay,
                label = getLabelForCycleDay(cycleDay),
                isToday = date == LocalDate.now()
            )
        }

        val todayDate = LocalDate.now()
        val todayCycleDay = calculateCycleDay(
            targetDate = todayDate,
            anchorEpochDay = config.cycleAnchorDateTimestamp,
            anchorCycleDay = config.cycleAnchorDay
        )
        val todayInfo = CalendarDayUiModel(
            date = todayDate,
            cycleDay = todayCycleDay,
            label = getLabelForCycleDay(todayCycleDay),
            isToday = true
        )

        CalendarUiState(
            days = calendarDays,
            todayInfo = todayInfo,
            selectedDate = selectedDate,
            workoutResults = results,
            currentMonth = month,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState(isLoading = true)
    )

    private fun getLabelForCycleDay(cycleDay: Int): String = when (cycleDay) {
        1 -> "Денна зміна"
        2 -> "Нічна зміна"
        3 -> "Відсипний"
        4 -> "Вихідний"
        else -> "Невідомо"
    }

    fun onDateSelected(date: LocalDate?) {
        _selectedDate.value = date
        if (date != null) {
            loadWorkoutResults(date)
        } else {
            _workoutResults.value = emptyList()
        }
    }

    fun onMonthChange(month: YearMonth) {
        _currentMonth.value = month
    }

    private fun loadWorkoutResults(date: LocalDate) {
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            analyticsRepo.getSessionsByDate(millis).collect { sessions ->
                val results = sessions.flatMap { sessionWithSets ->
                    sessionWithSets.sets.groupBy { it.exerciseId }.map { (exId, sets) ->
                        val name = workoutDao.getExerciseNameById(exId) ?: "Вправа $exId"
                        WorkoutResultUiModel(
                            exerciseName = name,
                            sets = sets.map { SetResultUiModel(it.weight, it.reps) }
                        )
                    }
                }
                _workoutResults.value = results
            }
        }
    }

    fun getScheduleForDay(cycleDay: Int) = scheduleRepo.getScheduleForDay(cycleDay)
}
