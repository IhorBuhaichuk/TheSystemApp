package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.CalendarCycleDay
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarDayCompletionStatus
import com.ihor.thesystem.domain.model.CalendarWeekDay
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.TodoItem
import com.ihor.thesystem.domain.repository.CalendarCycleRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.TodoRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetCalendarWeekPreviewUseCase @Inject constructor(
    private val calendarCycleRepository: CalendarCycleRepository,
    private val systemConfigRepository: SystemConfigRepository,
    private val playerRepository: PlayerRepository,
    private val scheduleRepository: ScheduleRepository,
    private val todoRepository: TodoRepository,
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val clock: AppClock
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<CalendarWeekDay>> {
        val today = today()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

        return combine(
            calendarCycleRepository.getCalendarCycle(),
            systemConfigRepository.getConfigFlow().map { it ?: SystemConfig() },
            playerRepository.getPlayer()
        ) { calendarCycle, trainingConfig, player ->
            Triple(calendarCycle, trainingConfig, player)
        }.flatMapLatest { (calendarCycle, trainingConfig, player) ->
            val dayFlows = weekDates.map { date ->
                val trainingCycleDay = calculateTrainingCycleDay(date, trainingConfig, player?.currentCycleDay)
                combine(
                    scheduleRepository.getScheduleForDay(trainingCycleDay),
                    todoRepository.getTodosForDate(date),
                    workoutAnalyticsRepository.getSessionsByDate(date.toStartOfDayMillis())
                ) { schedule, todos, sessions ->
                    val configuredCalendarDay = calendarCycle.dayForOrNull(date)
                    val calendarDay = configuredCalendarDay ?: CalendarCycleDay(
                        index = 0,
                        name = "",
                        type = CalendarCycleDayType.OFF
                    )
                    val isCalendarCycleConfigured = configuredCalendarDay != null
                    val hasTraining = schedule?.isWorkoutDay == true &&
                        (schedule.workoutTemplateName != null || schedule.exercises.isNotEmpty())
                    val allTodos = todos.flatMapWithMicrotasks()
                    val completedTasks = allTodos.count { it.isCompleted }
                    val totalTasks = allTodos.size
                    val hasCompletedWorkout = sessions.any { log ->
                        log.sets.any { it.isCompleted }
                    }

                    CalendarWeekDay(
                        date = date,
                        calendarDay = calendarDay,
                        trainingCycleDay = trainingCycleDay,
                        hasTraining = isCalendarCycleConfigured && hasTraining,
                        completionStatus = if (isCalendarCycleConfigured) {
                            resolveCompletionStatus(
                                date = date,
                                today = today,
                                hasTraining = hasTraining,
                                hasCompletedWorkout = hasCompletedWorkout,
                                completedTasks = completedTasks,
                                totalTasks = totalTasks
                            )
                        } else {
                            CalendarDayCompletionStatus.NO_DATA
                        },
                        completedTasks = if (isCalendarCycleConfigured) completedTasks else 0,
                        totalTasks = if (isCalendarCycleConfigured) totalTasks else 0,
                        isToday = date == today
                    )
                }
            }
            combine(dayFlows) { days -> days.toList() }
        }
    }

    private fun calculateTrainingCycleDay(
        date: LocalDate,
        config: SystemConfig,
        fallbackCurrentCycleDay: Int?
    ): Int = resolveTrainingCycleDay(
        targetDate = date,
        config = config,
        fallbackCurrentCycleDay = fallbackCurrentCycleDay
    )

    private fun resolveCompletionStatus(
        date: LocalDate,
        today: LocalDate,
        hasTraining: Boolean,
        hasCompletedWorkout: Boolean,
        completedTasks: Int,
        totalTasks: Int
    ): CalendarDayCompletionStatus =
        when {
            totalTasks == 0 && !hasTraining && !hasCompletedWorkout -> CalendarDayCompletionStatus.NO_DATA
            hasCompletedWorkout && (totalTasks == 0 || completedTasks >= totalTasks) -> CalendarDayCompletionStatus.COMPLETED
            totalTasks > 0 && completedTasks >= totalTasks && !hasTraining -> CalendarDayCompletionStatus.COMPLETED
            completedTasks > 0 || hasCompletedWorkout -> CalendarDayCompletionStatus.PARTIAL
            date.isBefore(today) && (totalTasks > 0 || hasTraining) -> CalendarDayCompletionStatus.MISSED
            else -> CalendarDayCompletionStatus.PLANNED
        }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private fun LocalDate.toStartOfDayMillis(): Long =
        atStartOfDay(clock.zoneId())
            .toInstant()
            .toEpochMilli()

    private fun List<TodoItem>.flatMapWithMicrotasks(): List<TodoItem> =
        flatMap { todo -> listOf(todo) + todo.microtasks }
}
