package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.ProgressionMatrixEntity
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import com.ihor.thesystem.domain.usecase.GetDailySummaryForDateUseCase
import com.ihor.thesystem.domain.usecase.CalendarLogItem
import com.ihor.thesystem.domain.usecase.LogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.usecase.SyncCycleAnchorUseCase
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.DayType
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarDayUiModel(
    val date: LocalDate,
    val cycleDay: Int,
    val label: String,
    val isToday: Boolean,
    val isActive: Boolean = false
)

data class CalendarUiState(
    val days: List<CalendarDayUiModel> = emptyList(),
    val cycleDays: List<CycleDayUiModel> = emptyList(),
    val todayInfo: CalendarDayUiModel? = null,
    val selectedDate: LocalDate? = null,
    val workoutResults: List<WorkoutResultUiModel> = emptyList(),
    val dailyLogs: List<CalendarLogItem> = emptyList(),
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
    private val viewingDateRepo: ViewingDateRepository,
    private val playerRepo: PlayerRepository,
    private val getDailySummary: GetDailySummaryForDateUseCase,
    private val syncCycleAnchor: SyncCycleAnchorUseCase
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val _selectedDate = viewingDateRepo.selectedDate
    private val _workoutResults = MutableStateFlow<List<WorkoutResultUiModel>>(emptyList())
    private val _dailyLogs = MutableStateFlow<List<CalendarLogItem>>(emptyList())
    private val _recommendations = MutableStateFlow<List<ProgressionMatrixEntry>>(emptyList())
    private val _loggedWeight = MutableStateFlow<Double?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        configRepo.getConfigFlow().filterNotNull(),
        _selectedDate,
        _workoutResults,
        _dailyLogs,
        _recommendations,
        _loggedWeight,
        playerRepo.getPlayer().filterNotNull()
    ) { args: Array<Any?> ->
        val month = args[0] as YearMonth
        val config = args[1] as SystemConfig
        val selectedDate = args[2] as LocalDate?
        val results = args[3] as List<WorkoutResultUiModel>
        val logs = args[4] as List<CalendarLogItem>
        val recs = args[5] as List<ProgressionMatrixEntry>
        val weight = args[6] as Double?
        val player = args[7] as com.ihor.thesystem.domain.model.Player

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
                isToday = date == LocalDate.now(),
                isActive = date == LocalDate.now()
            )
        }

        val todayDate = LocalDate.now()
        val todayCycleDay = calculateCycleDay(
            targetDate = todayDate,
            anchorEpochDay = config.cycleAnchorDateTimestamp,
            anchorCycleDay = config.cycleAnchorDay
        )

        val cycleDays = (1..4).map { d ->
            CycleDayUiModel(
                dayNumber = d,
                type = if (d <= 2) DayType.WORKOUT else DayType.REST,
                isActive = d == player.currentCycleDay,
                isSelected = false
            )
        }
        
        CalendarUiState(
            days = calendarDays,
            cycleDays = cycleDays,
            todayInfo = CalendarDayUiModel(todayDate, todayCycleDay, getLabelForCycleDay(todayCycleDay), true),
            selectedDate = selectedDate,
            workoutResults = results,
            dailyLogs = logs,
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

    private fun getLabelForCycleDay(day: Int): String {
        return when(day) {
            1 -> "ДЕННА ЗМІНА"
            2 -> "НІЧНА ЗМІНА"
            3 -> "ВІДСИПНИЙ"
            4 -> "ВИХІДНИЙ"
            else -> ""
        }
    }

    fun onMonthChange(month: YearMonth) { _currentMonth.value = month }
    fun onDateSelected(date: LocalDate?) {
        if (date != null) {
            viewingDateRepo.setDate(date)
            loadWorkoutResults(date)
            loadDailyLogs(date)
            loadRecommendations(date)
            loadWeight(date)
        } else {
            _loggedWeight.value = null
            _workoutResults.value = emptyList()
            _dailyLogs.value = emptyList()
            _recommendations.value = emptyList()
        }
    }

    private fun loadDailyLogs(date: LocalDate) {
        viewModelScope.launch {
            getDailySummary(date).collect { logs ->
                _dailyLogs.value = logs
            }
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

    private var recommendationsJob: Job? = null

    private fun loadRecommendations(date: LocalDate) {
        recommendationsJob?.cancel()
        recommendationsJob = viewModelScope.launch {
            val config = configRepo.getConfigFlow().firstOrNull() ?: return@launch
            val cycleDay = calculateCycleDay(
                targetDate = date,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
                anchorCycleDay = config.cycleAnchorDay
            )
            
            val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()
            if (schedule?.workoutTemplateId != null) {
                val exerciseIds = schedule.exercises.map { it.id }
                matrixRepo.getAllEntries().first().let { allEntries ->
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

    fun onConfirmSync(day: Int) {
        viewModelScope.launch {
            syncCycleAnchor(day)
        }
    }

    fun getScheduleForDay(day: Int) = scheduleRepo.getScheduleForDay(day)
}
