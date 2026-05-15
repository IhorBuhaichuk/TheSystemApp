package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.repository.WorkoutRepository
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetDailySummaryForDateUseCaseTest {

    private val questRepository: QuestRepository = mockk()
    private val analyticsRepository: WorkoutAnalyticsRepository = mockk()
    private val workoutRepository: WorkoutRepository = mockk()
    private val clock = object : AppClock {
        override fun now(): Long = TEST_TIMESTAMP
        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }
    private val useCase = GetDailySummaryForDateUseCase(
        questRepo = questRepository,
        analyticsRepo = analyticsRepository,
        workoutRepository = workoutRepository,
        clock = clock
    )

    @Test
    fun `bodyweight workout summary uses reps without fake kilogram load`() = runTest {
        givenDailyQuestsAreEmpty()
        givenExercises(
            ExerciseDetails(
                id = PUSH_UP_ID,
                name = "Push-up",
                trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS.name
            )
        )
        givenWorkoutLogs(
            ExerciseSet(
                setId = 1L,
                sessionId = SESSION_ID,
                exerciseId = PUSH_UP_ID,
                weight = 1.0,
                reps = 15,
                isCompleted = true
            ),
            ExerciseSet(
                setId = 2L,
                sessionId = SESSION_ID,
                exerciseId = PUSH_UP_ID,
                weight = 1.0,
                reps = 12,
                isCompleted = true
            )
        )

        val subtitle = useCase(TEST_DATE).first().single().subtitle

        assertTrue(subtitle.contains("Push-up"))
        assertTrue(subtitle.contains("15"))
        assertFalse(subtitle.contains("@"))
        assertFalse(subtitle.contains("кг") || subtitle.contains("РєРі") || subtitle.contains("kg"))
    }

    @Test
    fun `timed workout summary uses duration without fake kilogram load`() = runTest {
        givenDailyQuestsAreEmpty()
        givenExercises(
            ExerciseDetails(
                id = PLANK_ID,
                name = "Plank",
                trackingMode = ExerciseTrackingMode.TIME_SECONDS.name
            )
        )
        givenWorkoutLogs(
            ExerciseSet(
                setId = 1L,
                sessionId = SESSION_ID,
                exerciseId = PLANK_ID,
                weight = 1.0,
                reps = 60,
                isCompleted = true
            )
        )

        val subtitle = useCase(TEST_DATE).first().single().subtitle

        assertTrue(subtitle.contains("Plank"))
        assertTrue(subtitle.contains("60"))
        assertFalse(subtitle.contains("@"))
        assertFalse(subtitle.contains("кг") || subtitle.contains("РєРі") || subtitle.contains("kg"))
    }

    @Test
    fun `weighted workout summary keeps kilogram metric`() = runTest {
        givenDailyQuestsAreEmpty()
        givenExercises(
            ExerciseDetails(
                id = BENCH_ID,
                name = "Bench press",
                trackingMode = ExerciseTrackingMode.WEIGHT_REPS.name
            )
        )
        givenWorkoutLogs(
            ExerciseSet(
                setId = 1L,
                sessionId = SESSION_ID,
                exerciseId = BENCH_ID,
                weight = 60.0,
                reps = 8,
                isCompleted = true
            )
        )

        val subtitle = useCase(TEST_DATE).first().single().subtitle

        assertTrue(subtitle.contains("Bench press"))
        assertTrue(subtitle.contains("60"))
        assertTrue(subtitle.contains("x 8"))
        assertTrue(subtitle.contains("кг") || subtitle.contains("РєРі") || subtitle.contains("kg"))
    }

    private fun givenDailyQuestsAreEmpty() {
        every { questRepository.getQuestsByDate(any()) } returns flowOf(emptyList())
    }

    private fun givenExercises(vararg exercises: ExerciseDetails) {
        coEvery { workoutRepository.getAllExercisesSync() } returns exercises.toList()
    }

    private fun givenWorkoutLogs(vararg sets: ExerciseSet) {
        every { analyticsRepository.getSessionsByDate(any()) } returns flowOf(
            listOf(
                WorkoutLog(
                    session = WorkoutSession(
                        sessionId = SESSION_ID,
                        questId = 1L,
                        timestamp = TEST_TIMESTAMP,
                        totalTonnage = 0.0,
                        cycleDay = 1
                    ),
                    sets = sets.toList()
                )
            )
        )
    }
}

private val TEST_DATE = LocalDate.of(2026, 5, 14)
private const val TEST_TIMESTAMP = 1_779_000_000_000L
private const val SESSION_ID = 42L
private const val PUSH_UP_ID = 10
private const val PLANK_ID = 11
private const val BENCH_ID = 12
