package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BetaMetricsEventState
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class BetaMetricsAggregatorTest {

    @Test
    fun `aggregates local beta metrics from existing system signals`() {
        val monday = LocalDate.of(2026, 7, 6)
        val tuesday = monday.plusDays(1)
        val wednesday = monday.plusDays(2)
        val metrics = BetaMetricsAggregator.aggregate(
            onboardingCompleted = true,
            workoutLogs = listOf(workoutLog(monday), workoutLog(wednesday)),
            player = player(currentStreak = 4),
            schedulesByCycleDay = mapOf(
                1 to scheduleDay(cycleDay = 1, workout = true),
                2 to scheduleDay(cycleDay = 2, workout = true),
                3 to scheduleDay(cycleDay = 3, workout = true)
            ),
            eventState = BetaMetricsEventState(
                appOpenedEpochDays = setOf(monday.toEpochDay(), tuesday.toEpochDay(), wednesday.toEpochDay()),
                todayOrderDecisionsByDay = mapOf(
                    monday.toEpochDay() to TodayTrainingDecisionType.STANDARD_TRAINING,
                    tuesday.toEpochDay() to TodayTrainingDecisionType.REST,
                    wednesday.toEpochDay() to TodayTrainingDecisionType.STANDARD_TRAINING
                )
            ),
            today = wednesday,
            zoneId = TEST_ZONE,
            cycleDayForDate = { date ->
                when (date) {
                    monday -> 1
                    tuesday -> 2
                    wednesday -> 3
                    else -> 4
                }
            }
        )

        assertTrue(metrics.onboardingCompleted)
        assertTrue(metrics.firstWorkoutLogged)
        assertEquals(2, metrics.plannedWorkoutsCompletedThisWeek)
        assertEquals(1, metrics.plannedWorkoutsMissedThisWeek)
        assertEquals(4, metrics.currentStreak)
        assertEquals(3, metrics.daysAppOpenedOrRefreshed)
        assertEquals(2, metrics.todayOrderDecisionDistribution[TodayTrainingDecisionType.STANDARD_TRAINING])
        assertEquals(1, metrics.todayOrderDecisionDistribution[TodayTrainingDecisionType.REST])
    }

    @Test
    fun `planned workout for today is not missed before the day ends`() {
        val today = LocalDate.of(2026, 7, 8)
        val metrics = BetaMetricsAggregator.aggregate(
            onboardingCompleted = false,
            workoutLogs = emptyList(),
            player = player(currentStreak = 0),
            schedulesByCycleDay = mapOf(
                1 to scheduleDay(cycleDay = 1, workout = true),
                2 to scheduleDay(cycleDay = 2, workout = false)
            ),
            eventState = BetaMetricsEventState(),
            today = today,
            zoneId = TEST_ZONE,
            cycleDayForDate = { date -> if (date == today) 1 else 2 }
        )

        assertFalse(metrics.firstWorkoutLogged)
        assertEquals(0, metrics.plannedWorkoutsCompletedThisWeek)
        assertEquals(0, metrics.plannedWorkoutsMissedThisWeek)
    }

    private fun workoutLog(date: LocalDate): WorkoutLog =
        WorkoutLog(
            session = WorkoutSession(
                questId = 1L,
                timestamp = date.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli(),
                totalTonnage = 1_000.0,
                cycleDay = 1
            ),
            sets = emptyList()
        )

    private fun scheduleDay(cycleDay: Int, workout: Boolean): ScheduleDay =
        ScheduleDay(
            id = cycleDay,
            cycleDay = cycleDay,
            workoutTemplateId = if (workout) cycleDay else null,
            workoutTemplateName = if (workout) "Workout $cycleDay" else null,
            dailyTaskNames = emptyList(),
            exercises = emptyList()
        )

    private fun player(currentStreak: Int): Player =
        Player(
            id = 1,
            name = "Ihor",
            level = 1,
            playerClass = PlayerRank.NOVICE,
            height = 180f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = 1,
            globalRank = Rank.E,
            currentStreak = currentStreak
        )

    private companion object {
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
    }
}
