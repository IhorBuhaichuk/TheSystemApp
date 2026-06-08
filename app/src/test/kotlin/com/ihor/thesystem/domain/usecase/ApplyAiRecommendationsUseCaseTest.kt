package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AiRecommendationApplicationResult
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.model.MessageTextKey
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ApplyAiRecommendationsUseCaseTest {

    @Test
    fun `direct AI recommendation over allowed step is clamped before matrix update`() = runTest {
        val fixture = fixture(matrix = listOf(weightedEntry(currentWeight = 100f, targetWeight = 150f)))

        val result = fixture.useCase(
            listOf(
                AiWorkoutRecommendation(
                    exerciseId = EXERCISE_ID,
                    weight = 130f,
                    sets = 3,
                    reps = "8"
                )
            )
        )

        assertEquals(0, result.rejectedCount)
        assertEquals(1, result.clampedCount)
        coVerify {
            fixture.matrixRepository.updateTarget(
                exerciseId = EXERCISE_ID,
                weight = 102.5,
                sets = 3,
                reps = "8",
                aiFeedback = null,
                timestamp = NOW
            )
        }
    }

    @Test
    fun `direct AI recommendation cannot update bodyweight exercise with kg target`() = runTest {
        val fixture = fixture(
            matrix = listOf(
                weightedEntry(
                    exerciseName = "Push-up",
                    trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS,
                    currentWeight = 0f,
                    targetWeight = 0f
                )
            )
        )

        val result = fixture.useCase(
            listOf(
                AiWorkoutRecommendation(
                    exerciseId = EXERCISE_ID,
                    weight = 20f,
                    sets = 3,
                    reps = "12"
                )
            )
        )

        assertEquals(1, result.rejectedCount)
        coVerify(exactly = 0) {
            fixture.matrixRepository.updateTarget(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `direct AI recommendation is rejected during recovery block`() = runTest {
        val fixture = fixture(
            matrix = listOf(weightedEntry()),
            todayDecision = todayDecision(
                readinessLevel = ReadinessLevel.RECOVERY,
                readinessScore = 35,
                decisionType = TodayTrainingDecisionType.ACTIVE_RECOVERY,
                debtLevel = RecoveryDebtLevel.LOW
            )
        )

        val result = fixture.useCase(
            listOf(
                AiWorkoutRecommendation(
                    exerciseId = EXERCISE_ID,
                    weight = 102.5f,
                    sets = 3,
                    reps = "8"
                )
            )
        )

        assertEquals(1, result.rejectedCount)
        coVerify(exactly = 0) {
            fixture.matrixRepository.updateTarget(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `batch AI malformed response is non actionable and does not update matrix`() = runTest {
        val fixture = fixture(matrix = listOf(weightedEntry()))
        coEvery { fixture.aiRepository.getChatResponse(any()) } returns ChatMessage(
            role = ChatRole.AI,
            text = MessageText.Resource(MessageTextKey.ERROR_AI_PARSING),
            recommendations = listOf(
                AiWorkoutRecommendation(
                    exerciseId = EXERCISE_ID,
                    weight = 500f,
                    sets = 10,
                    reps = "30"
                )
            ),
            isActionable = false
        )

        val result = fixture.useCase(listOf(EXERCISE_ID))

        assertEquals(AiRecommendationApplicationResult.Empty, result)
        coVerify(exactly = 0) {
            fixture.matrixRepository.updateTarget(any(), any(), any(), any(), any(), any())
        }
    }

    private fun fixture(
        matrix: List<ProgressionMatrixEntry>,
        todayDecision: TodayTrainingDecision = todayDecision()
    ): Fixture {
        val matrixRepository = mockk<ProgressionMatrixRepository>(relaxed = true)
        val playerRepository = mockk<PlayerRepository>(relaxed = true)
        val analyticsRepository = mockk<WorkoutAnalyticsRepository>(relaxed = true)
        val aiRepository = mockk<AiArchitectRepository>(relaxed = true)
        val getWeightContext = mockk<GetPlayerWeightContextUseCase>(relaxed = true)
        val getTrainingPhaseContext = mockk<GetTrainingPhaseContextUseCase>(relaxed = true)
        val decideTodayWorkout = mockk<DecideTodayWorkoutUseCase>()

        every { matrixRepository.getAllEntries() } returns flowOf(matrix)
        every { analyticsRepository.getAllLogs() } returns flowOf(emptyList())
        coEvery { analyticsRepository.getRecentLogsForExercise(any()) } returns emptyList()
        coEvery { getWeightContext() } returns PlayerWeightContext(currentWeight = null, weightSixMonthsAgo = null)
        coEvery { getTrainingPhaseContext(null) } returns TrainingPhaseContext(
            firstWorkoutDate = null,
            referenceDate = TODAY
        )
        coEvery { decideTodayWorkout(any()) } returns todayDecision

        val useCase = ApplyAiRecommendationsUseCase(
            matrixRepo = matrixRepository,
            playerRepo = playerRepository,
            analyticsRepo = analyticsRepository,
            aiRepository = aiRepository,
            getWeightContext = getWeightContext,
            getTrainingPhaseContext = getTrainingPhaseContext,
            validateDirectives = ValidateDirectivesUseCase(),
            decideTodayWorkout = decideTodayWorkout,
            transactionProvider = ImmediateTransactionProvider,
            clock = FixedClock,
            logger = mockk(relaxed = true)
        )

        return Fixture(useCase, matrixRepository, aiRepository)
    }

    private data class Fixture(
        val useCase: ApplyAiRecommendationsUseCase,
        val matrixRepository: ProgressionMatrixRepository,
        val aiRepository: AiArchitectRepository
    )

    private fun weightedEntry(
        exerciseName: String = "Bench Press",
        trackingMode: ExerciseTrackingMode = ExerciseTrackingMode.WEIGHT_REPS,
        currentWeight: Float = 100f,
        targetWeight: Float = 150f
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = EXERCISE_ID,
            exerciseName = exerciseName,
            exerciseNameUk = null,
            exerciseTrackingMode = trackingMode.name,
            startWeight = 80f,
            targetWeight = targetWeight,
            currentWeight = currentWeight,
            targetWeightNote = null,
            weeklyStep = 2.5f,
            progressPercent = 0f
        )

    private fun todayDecision(
        readinessLevel: ReadinessLevel = ReadinessLevel.STANDARD,
        readinessScore: Int = 75,
        decisionType: TodayTrainingDecisionType = TodayTrainingDecisionType.STANDARD_TRAINING,
        debtLevel: RecoveryDebtLevel = RecoveryDebtLevel.LOW
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = TODAY.toEpochDay(),
            cycleDay = 1,
            workoutName = "Upper Day",
            readinessScore = readinessScore,
            readinessLevel = readinessLevel,
            recoveryDebt = RecoveryDebt(
                value = 10,
                level = debtLevel,
                reasons = emptyList()
            ),
            decisionType = decisionType,
            loadMultiplier = 1f,
            volumeMultiplier = 1f,
            reason = "Test",
            warnings = emptyList(),
            selectedWorkoutTemplateId = 1,
            isTrainingAllowed = decisionType == TodayTrainingDecisionType.STANDARD_TRAINING
        )

    private object ImmediateTransactionProvider : TransactionProvider {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()
    }

    private object FixedClock : AppClock {
        override fun now(): Long = NOW
        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }

    private companion object {
        const val EXERCISE_ID = 10
        const val NOW = 1_780_000_000_000L
        val TODAY: LocalDate = LocalDate.of(2026, 6, 8)
    }
}
