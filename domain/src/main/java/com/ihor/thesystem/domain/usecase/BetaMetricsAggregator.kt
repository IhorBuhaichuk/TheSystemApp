package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BetaMetrics
import com.ihor.thesystem.domain.model.BetaMetricsEventState
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutLog
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object BetaMetricsAggregator {
    fun aggregate(
        onboardingCompleted: Boolean,
        workoutLogs: List<WorkoutLog>,
        player: Player?,
        schedulesByCycleDay: Map<Int, ScheduleDay>,
        eventState: BetaMetricsEventState,
        today: LocalDate,
        zoneId: ZoneId,
        cycleDayForDate: (LocalDate) -> Int
    ): BetaMetrics {
        val workoutDates = workoutLogs
            .map { it.session.timestamp.toLocalDate(zoneId) }
            .toSet()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekToDate = generateSequence(weekStart) { date ->
            date.plusDays(1).takeIf { it <= today }
        }.toList()

        val plannedWeekDates = weekToDate.filter { date ->
            schedulesByCycleDay[cycleDayForDate(date)]?.isWorkoutDay == true
        }
        val completed = plannedWeekDates.count { it in workoutDates }
        val missed = plannedWeekDates.count { it < today && it !in workoutDates }
        val decisionDistribution = TodayTrainingDecisionType.entries.associateWith { type ->
            eventState.todayOrderDecisionsByDay.values.count { it == type }
        }

        return BetaMetrics(
            onboardingCompleted = onboardingCompleted,
            firstWorkoutLogged = workoutLogs.isNotEmpty(),
            plannedWorkoutsCompletedThisWeek = completed,
            plannedWorkoutsMissedThisWeek = missed,
            currentStreak = player?.currentStreak ?: 0,
            daysAppOpenedOrRefreshed = eventState.appOpenedEpochDays.size,
            todayOrderDecisionDistribution = decisionDistribution
        )
    }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}
