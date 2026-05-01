package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.CalendarCycleDay
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarDayCompletionStatus
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.GetDailySummaryForDateUseCase
import com.ihor.thesystem.domain.usecase.GetTodosForDateUseCase
import com.ihor.thesystem.domain.usecase.GetTodoStatsForMonthUseCase
import com.ihor.thesystem.domain.usecase.CalendarLogItem
import com.ihor.thesystem.domain.usecase.ResolveTrainingCycleDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.ihor.thesystem.domain.usecase.SyncCycleAnchorUseCase
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.DayType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarDayUiModel(
    val date: LocalDate,
    val cycleDay: Int,
    val label: String,
    val isToday: Boolean,
    val isCalendarCycleConfigured: Boolean = true,
    val calendarDayType: CalendarCycleDayType = CalendarCycleDayType.OFF,
    val isActive: Boolean = false,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val hasTrainingPlan: Boolean = false,
    val plannedWorkoutName: String? = null,
    val plannedExerciseCount: Int = 0,
    val hasCompletedWorkout: Boolean = false,
    val completionStatus: CalendarDayCompletionStatus = CalendarDayCompletionStatus.NO_DATA
)

data class DailyTaskSnapshotUiModel(
    val completedTasks: List<String>,   // назви виконаних завдань
    val failedTasks: List<String>,      // назви НЕ виконаних завдань
    val completedPercent: Int,          // округлений відсоток виконання
    val failedPercent: Int              // округлений відсоток невиконання
) {
    val hasAnyData: Boolean get() = completedTasks.isNotEmpty() || failedTasks.isNotEmpty()

    companion object {
        val Empty = DailyTaskSnapshotUiModel(
            completedTasks = emptyList(),
            failedTasks = emptyList(),
            completedPercent = 0,
            failedPercent = 0
        )
    }
}

data class CalendarUiState(
    val days: List<CalendarDayUiModel> = emptyList(),
    val cycleDays: List<CycleDayUiModel> = emptyList(),
    val todayInfo: CalendarDayUiModel? = null,
    val selectedDate: LocalDate? = null,
    val workoutResults: List<WorkoutResultUiModel> = emptyList(),
    val dailyLogs: List<CalendarLogItem> = emptyList(),
    val dailyTaskSnapshot: DailyTaskSnapshotUiModel = DailyTaskSnapshotUiModel.Empty,
    val nextWorkoutRecommendations: List<ProgressionMatrixEntry> = emptyList(),
    val loggedWeightForDate: Double? = null,
    val currentMonth: YearMonth = YearMonth.now(),
    val playerName: String = "TheSystem",
    val playerRank: PlayerRank = PlayerRank.NOVICE,
    val globalRank: Rank = Rank.E,
    val currentStreak: Int = 0,
    val xpThisWeek: Int = 0,
    val avatarUri: String? = null,
    val isLoading: Boolean = false
)

data class WorkoutResultUiModel(
    val exerciseId: Int,
    val exerciseName: String,
    val sets: List<SetResultUiModel>
)

data class SetResultUiModel(
    val weight: Double,
    val reps: Int
)

private data class CalendarSelectionData(
    val workoutResults: List<WorkoutResultUiModel>,
    val dailyLogs: List<CalendarLogItem>,
    val dailyTaskSnapshot: DailyTaskSnapshotUiModel,
    val recommendations: List<ProgressionMatrixEntry>,
    val loggedWeight: Double?
)

private data class CalendarBaseData(
    val monthData: Triple<YearMonth, Map<LocalDate, Pair<Int, Int>>, Set<LocalDate>>,
    val config: SystemConfig,
    val selectedDate: LocalDate?,
    val selectionData: CalendarSelectionData,
    val player: Player
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val workoutRepo: WorkoutRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val viewingDateRepo: ViewingDateRepository,
    private val playerRepo: PlayerRepository,
    private val getTodosForDate: GetTodosForDateUseCase,
    private val getTodoStatsForMonth: GetTodoStatsForMonthUseCase,
    private val calendarCycleRepository: CalendarCycleRepository,
    private val getDailySummary: GetDailySummaryForDateUseCase,
    private val syncCycleAnchor: SyncCycleAnchorUseCase,
    private val clock: AppClock
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.from(today()))
    val _selectedDate = viewingDateRepo.selectedDate
    private val _workoutResults = MutableStateFlow<List<WorkoutResultUiModel>>(emptyList())
    private val _dailyLogs = MutableStateFlow<List<CalendarLogItem>>(emptyList())
    private val _dailyTaskSnapshot = MutableStateFlow(DailyTaskSnapshotUiModel.Empty)
    private val _recommendations = MutableStateFlow<List<ProgressionMatrixEntry>>(emptyList())
    private val _loggedWeight = MutableStateFlow<Double?>(null)
    private val _monthTaskStats = MutableStateFlow<Map<LocalDate, Pair<Int, Int>>>(emptyMap())
    private val _monthWorkoutDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    private var monthWorkoutJob: Job? = null
    private var dailyLogsJob: Job? = null
    private var dailyTaskSnapshotJob: Job? = null
    private var workoutResultsJob: Job? = null
    private var weightJob: Job? = null

    init {
        viewModelScope.launch {
            // Автоматично реагуємо на зміну обраної дати і завантажуємо дані
            _selectedDate.collectLatest { date ->
                if (date != null) {
                    loadWorkoutResults(date)
                    loadDailyLogs(date)
                    loadDailyTaskSnapshot(date)
                    loadRecommendations(date)
                    loadWeight(date)
                } else {
                    _loggedWeight.value = null
                    _workoutResults.value = emptyList()
                    _dailyLogs.value = emptyList()
                    _dailyTaskSnapshot.value = DailyTaskSnapshotUiModel.Empty
                    _recommendations.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            _currentMonth.collectLatest { month ->
                loadMonthTaskStats(month)
                observeMonthWorkoutDates(month)
            }
        }

        // Встановлюємо сьогоднішню дату за замовчуванням при першому відкритті, якщо дата не встановлена
        viewModelScope.launch {
            if (_selectedDate.firstOrNull() == null) {
                viewingDateRepo.setDate(today())
            }
        }
    }

    private val selectionData: Flow<CalendarSelectionData> = combine(
        _workoutResults,
        _dailyLogs,
        _dailyTaskSnapshot,
        _recommendations,
        _loggedWeight
    ) { results, logs, snapshot, recs, weight ->
        CalendarSelectionData(
            workoutResults = results,
            dailyLogs = logs,
            dailyTaskSnapshot = snapshot,
            recommendations = recs,
            loggedWeight = weight
        )
    }

    private val monthWithStats: Flow<Pair<YearMonth, Map<LocalDate, Pair<Int, Int>>>> = combine(
        _currentMonth,
        _monthTaskStats
    ) { month, stats ->
        month to stats
    }

    private val monthCalendarData: Flow<Triple<YearMonth, Map<LocalDate, Pair<Int, Int>>, Set<LocalDate>>> = combine(
        monthWithStats,
        _monthWorkoutDates
    ) { monthData, workoutDates ->
        Triple(monthData.first, monthData.second, workoutDates)
    }

    private val baseData: Flow<CalendarBaseData> = combine(
        monthCalendarData,
        configRepo.getConfigFlow().filterNotNull(),
        _selectedDate,
        selectionData,
        playerRepo.getPlayer().filterNotNull()
    ) { monthData, config, selectedDate, data, player ->
        CalendarBaseData(
            monthData = monthData,
            config = config,
            selectedDate = selectedDate,
            selectionData = data,
            player = player
        )
    }

    val uiState: StateFlow<CalendarUiState> = combine(
        baseData,
        calendarCycleRepository.getCalendarCycle()
    ) { baseData, calendarCycle ->
        val month = baseData.monthData.first
        val monthTaskStats = baseData.monthData.second
        val monthWorkoutDates = baseData.monthData.third
        val config = baseData.config
        val selectedDate = baseData.selectedDate
        val data = baseData.selectionData
        val player = baseData.player
        val daysInMonth = month.lengthOfMonth()
        val todayDate = today()

        val datesInMonth = (1..daysInMonth).map { month.atDay(it) }
        val cycleDayByDate = datesInMonth.associateWith { date ->
            resolveTrainingCycleDay(
                targetDate = date,
                config = config,
                fallbackCurrentCycleDay = player.currentCycleDay
            )
        }
        val schedulesByCycleDay = scheduleRepo
            .getSchedulesForDays(cycleDayByDate.values.distinct())
            .firstOrNull()
            .orEmpty()
            .associateBy { it.cycleDay }
        
        val calendarDays = datesInMonth.map { date ->
            val cycleDay = cycleDayByDate.getValue(date)
            val configuredCalendarDay = calendarCycle.dayForOrNull(date)
            val calendarDay = configuredCalendarDay ?: CalendarCycleDay(
                index = 0,
                name = "Налаштуйте цикл",
                type = CalendarCycleDayType.OFF
            )
            val isCalendarCycleConfigured = configuredCalendarDay != null
            val schedule = schedulesByCycleDay[cycleDay]
            val hasTraining = schedule?.isWorkoutDay == true &&
                (schedule.workoutTemplateName != null || schedule.exercises.isNotEmpty())
            val completedTasks = monthTaskStats[date]?.first ?: 0
            val totalTasks = monthTaskStats[date]?.second ?: 0
            val hasCompletedWorkout = date in monthWorkoutDates
            CalendarDayUiModel(
                date = date,
                cycleDay = cycleDay,
                label = calendarDay.name,
                isToday = date == todayDate,
                isCalendarCycleConfigured = isCalendarCycleConfigured,
                calendarDayType = calendarDay.type,
                isActive = date == todayDate,
                completedTasks = if (isCalendarCycleConfigured) completedTasks else 0,
                totalTasks = if (isCalendarCycleConfigured) totalTasks else 0,
                hasTrainingPlan = isCalendarCycleConfigured && hasTraining,
                plannedWorkoutName = schedule?.workoutTemplateName,
                plannedExerciseCount = schedule?.exercises.orEmpty().size,
                hasCompletedWorkout = isCalendarCycleConfigured && hasCompletedWorkout,
                completionStatus = resolveCompletionStatus(
                    date = date,
                    today = todayDate,
                    hasTraining = isCalendarCycleConfigured && hasTraining,
                    hasCompletedWorkout = isCalendarCycleConfigured && hasCompletedWorkout,
                    completedTasks = if (isCalendarCycleConfigured) completedTasks else 0,
                    totalTasks = if (isCalendarCycleConfigured) totalTasks else 0
                )
            )
        }

        val todayCycleDay = resolveTrainingCycleDay(
            targetDate = todayDate,
            config = config,
            fallbackCurrentCycleDay = player.currentCycleDay
        )
        val todayCalendarDay = calendarCycle.dayForOrNull(todayDate)

        val cycleDays = config.cycleDaysPerMicrocycle.let { total ->
            (1..total).map { d ->
                CycleDayUiModel(
                    dayNumber = d,
                    type = DayType.WORKOUT, // Default to WORKOUT, but logic below handles it
                    isActive = d == todayCycleDay,
                    isSelected = false
                )
            }
        }
        
        CalendarUiState(
            days = calendarDays,
            cycleDays = cycleDays,
            todayInfo = CalendarDayUiModel(
                date = todayDate,
                cycleDay = todayCycleDay,
                label = todayCalendarDay?.name ?: "Налаштуйте цикл",
                isToday = true,
                isCalendarCycleConfigured = todayCalendarDay != null,
                calendarDayType = todayCalendarDay?.type ?: CalendarCycleDayType.OFF
            ),
            selectedDate = selectedDate,
            workoutResults = data.workoutResults,
            dailyLogs = data.dailyLogs,
            dailyTaskSnapshot = data.dailyTaskSnapshot,
            nextWorkoutRecommendations = data.recommendations,
            loggedWeightForDate = data.loggedWeight,
            currentMonth = month,
            playerName = player.name,
            playerRank = player.playerClass,
            globalRank = player.globalRank,
            currentStreak = player.currentStreak,
            xpThisWeek = player.xpThisWeek,
            avatarUri = player.avatarUri,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState(isLoading = true)
    )

    fun onMonthChange(month: YearMonth) { _currentMonth.value = month }
    fun onDateSelected(date: LocalDate?) {
        viewingDateRepo.setDate(date)
    }

    private fun loadDailyLogs(date: LocalDate) {
        dailyLogsJob?.cancel()
        dailyLogsJob = viewModelScope.launch {
            getDailySummary(date).collect { logs ->
                _dailyLogs.value = logs
            }
        }
    }

    private fun loadDailyTaskSnapshot(date: LocalDate) {
        dailyTaskSnapshotJob?.cancel()
        dailyTaskSnapshotJob = viewModelScope.launch {
            getTodosForDate(date).collectLatest { tasks ->
                val completed = tasks.filter { it.isCompleted }.map { it.title }
                val failed = tasks.filter { !it.isCompleted }.map { it.title }

                val total = tasks.size
                val completedPercent = if (total > 0) (completed.size * 100 / total) else 0
                val failedPercent = if (total > 0) (failed.size * 100 / total) else 0

                _dailyTaskSnapshot.value = DailyTaskSnapshotUiModel(
                    completedTasks = completed,
                    failedTasks = failed,
                    completedPercent = completedPercent,
                    failedPercent = failedPercent
                )
            }
        }
    }

    private fun loadWorkoutResults(date: LocalDate) {
        val millis = date.toStartOfDayMillis()
        workoutResultsJob?.cancel()
        workoutResultsJob = viewModelScope.launch {
            analyticsRepo.getSessionsByDate(millis).collect { sessions ->
                val results = sessions.flatMap { session ->
                    session.sets.filter { it.isCompleted }.groupBy { it.exerciseId }.map { (id, sets) ->
                        WorkoutResultUiModel(
                            id,
                            workoutRepo.getExerciseNameById(id) ?: "Вправа",
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
            val cycleDay = resolveTrainingCycleDay(
                targetDate = date,
                config = config,
                fallbackCurrentCycleDay = playerRepo.getPlayer().firstOrNull()?.currentCycleDay
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
        val millis = date.toStartOfDayMillis()
        weightJob?.cancel()
        weightJob = viewModelScope.launch {
            _loggedWeight.value = when (val result = playerRepo.getWeightByDate(millis)) {
                is Result.Success -> result.data?.toDouble()
                is Result.Error -> null
            }
        }
    }

    fun onConfirmSync(day: Int) {
        viewModelScope.launch {
            syncCycleAnchor(day)
        }
    }

    private suspend fun loadMonthTaskStats(month: YearMonth) {
        _monthTaskStats.value = getTodoStatsForMonth(month).mapValues { (_, stats) ->
            stats.completedCount to stats.totalCount
        }
    }

    private fun observeMonthWorkoutDates(month: YearMonth) {
        monthWorkoutJob?.cancel()
        monthWorkoutJob = viewModelScope.launch {
            val start = month.atDay(1).toStartOfDayMillis()
            val end = month.atEndOfMonth().plusDays(1).toStartOfDayMillis() - 1
            analyticsRepo.getDailyTonnageStatsForMonth(start, end).collectLatest { stats ->
                _monthWorkoutDates.value = stats.map { entry ->
                    Instant.ofEpochMilli(entry.dateUnixTimestamp)
                        .atZone(clock.zoneId())
                        .toLocalDate()
                }.toSet()
            }
        }
    }

    fun getScheduleForDay(day: Int) = scheduleRepo.getScheduleForDay(day)

    private fun resolveCompletionStatus(
        date: LocalDate,
        today: LocalDate,
        hasTraining: Boolean,
        hasCompletedWorkout: Boolean,
        completedTasks: Int,
        totalTasks: Int
    ): CalendarDayCompletionStatus {
        val hasTasks = totalTasks > 0
        val allTasksCompleted = hasTasks && completedTasks >= totalTasks
        return when {
            !hasTasks && !hasTraining && !hasCompletedWorkout -> CalendarDayCompletionStatus.NO_DATA
            hasCompletedWorkout && (!hasTasks || allTasksCompleted) -> CalendarDayCompletionStatus.COMPLETED
            allTasksCompleted && !hasTraining -> CalendarDayCompletionStatus.COMPLETED
            completedTasks > 0 || hasCompletedWorkout -> CalendarDayCompletionStatus.PARTIAL
            date.isBefore(today) && (hasTasks || hasTraining) -> CalendarDayCompletionStatus.MISSED
            else -> CalendarDayCompletionStatus.PLANNED
        }
    }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private fun LocalDate.toStartOfDayMillis(): Long =
        atStartOfDay(clock.zoneId())
            .toInstant()
            .toEpochMilli()
}
