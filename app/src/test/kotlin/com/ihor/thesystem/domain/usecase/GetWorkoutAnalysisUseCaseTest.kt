package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.MotivationComponentScores
import com.ihor.thesystem.domain.model.MotivationLevel
import com.ihor.thesystem.domain.model.MotivationLevelResult
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.StrengthBenchmarkConfigSource
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetWorkoutAnalysisUseCaseTest {

    private val analyticsRepository: WorkoutAnalyticsRepository = mockk(relaxed = true)
    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val progressionMatrixRepository: ProgressionMatrixRepository = mockk(relaxed = true)
    private val playerRepository: PlayerRepository = mockk(relaxed = true)
    private val getSystemConfig: GetSystemConfigUseCase = mockk()
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase = mockk(relaxed = true)
    private val calculateRecommendation: CalculateRecommendedSetUseCase = mockk(relaxed = true)
    private val calculateMotivationLevel: CalculateMotivationLevelUseCase = mockk()
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase = mockk()
    private val clock = object : AppClock {
        override fun now(): Long = TEST_TIMESTAMP
        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }

    private val useCase = GetWorkoutAnalysisUseCase(
        analyticsRepository = analyticsRepository,
        scheduleRepository = scheduleRepository,
        progressionMatrixRepository = progressionMatrixRepository,
        playerRepository = playerRepository,
        strengthBenchmarkConfigSource = StrengthBenchmarkConfigSource(),
        getSystemConfig = getSystemConfig,
        resolveTrainingCycleDay = resolveTrainingCycleDay,
        calculateRecommendation = calculateRecommendation,
        calculateMotivationLevel = calculateMotivationLevel,
        getTrainingPhaseContext = getTrainingPhaseContext,
        clock = clock
    )

    @Test
    fun `bodyweight session counts execution without fake one kilogram strength analysis`() = runTest {
        val log = WorkoutLog(
            session = WorkoutSession(
                sessionId = 42L,
                questId = 7L,
                timestamp = TEST_TIMESTAMP,
                totalTonnage = 0.0,
                cycleDay = 1
            ),
            sets = listOf(
                bodyweightSet(setId = 1L, reps = 15),
                bodyweightSet(setId = 2L, reps = 12)
            )
        )
        every { analyticsRepository.getSessionById(42L) } returns flowOf(log)
        every { analyticsRepository.getAllLogs() } returns flowOf(listOf(log))
        coEvery { analyticsRepository.getAllExercisesMap() } returns mapOf(10 to "Push-up")
        every { progressionMatrixRepository.getAllEntries() } returns flowOf(
            listOf(
                ProgressionMatrixEntry(
                    id = 10,
                    exerciseId = 10,
                    exerciseName = "Push-up",
                    exerciseTrackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS.name,
                    startWeight = 0f,
                    targetWeight = 0f,
                    currentWeight = 0f,
                    targetWeightNote = null,
                    weeklyStep = 0f,
                    progressPercent = 0f,
                    nextRecommendedWeight = 1.0,
                    nextRecommendedSets = 3,
                    nextRecommendedReps = "15",
                    lastAiFeedback = "Add kilograms next time",
                    lastAnalyzedTimestamp = TEST_TIMESTAMP
                )
            )
        )
        every { scheduleRepository.getScheduleForDay(1) } returns flowOf(
            ScheduleDay(
                id = 1,
                cycleDay = 1,
                workoutTemplateId = 1,
                workoutTemplateName = "Bodyweight Day",
                dailyTaskNames = emptyList(),
                exercises = listOf(
                    ExerciseDetails(
                        id = 10,
                        name = "Push-up",
                        trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS.name
                    )
                )
            )
        )
        every { getSystemConfig.invoke() } returns flowOf(null)
        every { playerRepository.getLatestWeight() } returns flowOf(null)
        coEvery { getTrainingPhaseContext.invoke(TEST_TIMESTAMP) } returns TrainingPhaseContext(
            firstWorkoutDate = LocalDate.of(2026, 5, 14),
            referenceDate = LocalDate.of(2026, 5, 14)
        )
        every {
            calculateMotivationLevel.invoke(any(), any(), any())
        } returns MotivationLevelResult(
            finalScore = 50,
            level = MotivationLevel.STABLE,
            title = "Stable",
            description = "Neutral",
            componentScores = MotivationComponentScores(
                personalProgressScore = 50,
                planProgressScore = 50,
                consistencyScore = 50,
                strengthBenchmarkScore = 50
            )
        )

        val analysis = useCase(sessionId = 42L)

        assertNotNull(analysis)
        requireNotNull(analysis)
        assertEquals(2, analysis.execution.completedSets)
        assertEquals(1, analysis.execution.completedExercises)
        assertTrue(analysis.exerciseProgress.isEmpty())
        assertTrue(analysis.annualProgress.isEmpty())
        assertTrue(analysis.recommendations.isEmpty())
        assertEquals(null, analysis.aiFeedback)
        coVerify(exactly = 0) { calculateRecommendation.fromSets(any(), any(), any()) }
    }

    private fun bodyweightSet(setId: Long, reps: Int): ExerciseSet =
        ExerciseSet(
            setId = setId,
            sessionId = 42L,
            exerciseId = 10,
            weight = 1.0,
            reps = reps,
            isCompleted = true
        )
}

private const val TEST_TIMESTAMP = 1_779_000_000_000L
