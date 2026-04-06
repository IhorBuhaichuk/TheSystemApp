package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.ProgressionMatrixEntity
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarDayUiModel(
    val date: LocalDate,
    val cycleDay: Int,
    val label: String,
    val isToday: Boolean
)

data class CalendarUiState(
    val days: List<CalendarDayUiModel> = emptyList(),
    val todayInfo: CalendarDayUiModel? = null,
    val selectedDate: LocalDate? = null,
    val workoutResults: List<WorkoutResultUiModel> = emptyList(),
    val nextWorkoutRecommendations: List<ProgressionMatrixEntry> = emptyList(),
    val loggedWeightForDate: Double? = null,
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
    private val matrixRepo: ProgressionMatrixRepository,
    private val weightLogDao: WeightLogDao,
    private val workoutDao: WorkoutDao,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase,
    private val viewingDateRepo: ViewingDateRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val _selectedDate = viewingDateRepo.selectedDate
    private val _workoutResults = MutableStateFlow<List<WorkoutResultUiModel>>(emptyList())
    private val _recommendations = MutableStateFlow<List<ProgressionMatrixEntry>>(emptyList())
    private val _loggedWeight = MutableStateFlow<Double?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        configRepo.getConfigFlow().filterNotNull(),
        _selectedDate,
        _workoutResults,
        _recommendations,
        _loggedWeight
    ) { args: Array<Any?> ->
        val month = args[0] as YearMonth
        val config = args[1] as SystemConfig
        val selectedDate = args[2] as LocalDate?
        val results = args[3] as List<WorkoutResultUiModel>
        val recs = args[4] as List<ProgressionMatrixEntry>
        val weight = args[5] as Double?

        val daysInMonth = month.lengthOfMonth()
        
        val calendarDays = (1..daysInMonth).map { dayNum ->
            val date = month.atDay(dayNum)
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
        
        CalendarUiState(
            days = calendarDays,
            todayInfo = CalendarDayUiModel(todayDate, todayCycleDay, getLabelForCycleDay(todayCycleDay), true),
            selectedDate = selectedDate,
            workoutResults = results,
            nextWorkoutRecommendations = recs,
            loggedWeightForDate = weight,
            currentMonth = month,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState(isLoading = true)
    )

    private fun getLabelForCycleDay(day: Int) = when(day) {
        1 -> "Денна зміна"
        2 -> "Нічна зміна"
        3 -> "Відсипний"
        4 -> "Вихідний"
        else -> ""
    }

    fun onMonthChange(month: YearMonth) { _currentMonth.value = month }
    fun onDateSelected(date: LocalDate?) {
        if (date != null) {
            viewingDateRepo.setDate(date)
            loadWorkoutResults(date)
            loadRecommendations(date)
            loadWeight(date)
        } else {
            _loggedWeight.value = null
            _workoutResults.value = emptyList()
            _recommendations.value = emptyList()
        }
    }

    private fun loadWorkoutResults(date: LocalDate) {
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            analyticsRepo.getSessionsByDate(millis).collect { sessions ->
                val results = sessions.flatMap { session ->
                    session.sets.groupBy { it.exerciseId }.map { (id, sets) ->
                        WorkoutResultUiModel(
                            workoutDao.getExerciseNameById(id) ?: "Вправа",
                            sets.map { SetResultUiModel(it.weight, it.reps) }
                        )
                    }
                }
                _workoutResults.value = results
            }
        }
    }

    private fun loadRecommendations(date: LocalDate) {
        viewModelScope.launch {
            val config = configRepo.getConfigFlow().firstOrNull() ?: return@launch
            val cycleDay = calculateCycleDay(
                targetDate = date,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
                anchorCycleDay = config.cycleAnchorDay
            )
            
            val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()
            if (schedule?.workoutTemplateId != null) {
                val exerciseIds = schedule.exercises.map { it.id }
                matrixRepo.getAllEntries().collect { allEntries ->
                    _recommendations.value = allEntries.filter { 
                        exerciseIds.contains(it.exerciseId) && it.nextRecommendedWeight != null 
                    }
                }
            } else {
                _recommendations.value = emptyList()
            }
        }
    }

    private fun loadWeight(date: LocalDate) {
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            val weight = weightLogDao.getWeightByDate(millis)
            _loggedWeight.value = weight?.toDouble()
        }
    }

    fun getScheduleForDay(day: Int) = scheduleRepo.getScheduleForDay(day)
}
