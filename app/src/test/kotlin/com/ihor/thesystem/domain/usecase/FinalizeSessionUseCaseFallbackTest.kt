package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DirectiveValidationResult
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.model.MessageTextKey
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutProgressionDecision
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.Result
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

class FinalizeSessionUseCaseFallbackTest {

    private val analyticsRepository: WorkoutAnalyticsRepository = mockk(relaxed = true)
    private val sendArchitectAnalysis: SendArchitectAnalysisUseCase = mockk()
    private val progressionMatrixRepository: ProgressionMatrixRepository = mockk(relaxed = true)
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase = mockk()
    private val validateDirectives: ValidateDirectivesUseCase = mockk()
    private val transactionProvider: TransactionProvider = mockk()
    private val getWeightContext: GetPlayerWeightContextUseCase = mockk()
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase = mockk()
    private val questRepository: QuestRepository = mockk(relaxed = true)
    private val completeQuest: CompleteQuestUseCase = mockk(relaxed = true)
    private val calculateProgressRank: CalculateProgressRankUseCase = mockk()
    private val calculateWorkoutJudgment = CalculateWorkoutJudgmentUseCase()
    private val decideTodayWorkout: DecideTodayWorkoutUseCase = mockk()
    private val logger: AppLogger = mockk(relaxed = true)
    private val clock = FixedClock(LocalDate.of(2026, 1, 10))

    @Test
    fun `ai failure still returns local system verdict fallback report`() = runTest {
        val matrix = listOf(matrixEntry())
        val sets = listOf(
            completedSet(),
            completedSet(),
            completedSet()
        )
        coEvery { transactionProvider.runInTransaction<Any?>(any()) } coAnswers {
            firstArg<suspend () -> Any?>().invoke()
        }
        coEvery { analyticsRepository.saveFullSessionLog(any(), any()) } returns SESSION_ID
        every { progressionMatrixRepository.getAllEntries() } returns flowOf(matrix)
        coEvery { recalculateGlobalRank.invoke() } returns Unit
        every {
            calculateProgressRank.invoke(
                currentWeight = any(),
                startWeight = any(),
                targetWeight = any()
            )
        } returns null
        coEvery { getWeightContext.invoke() } returns PlayerWeightContext(
            currentWeight = null,
            weightSixMonthsAgo = null
        )
        coEvery { getTrainingPhaseContext.invoke(any()) } returns TrainingPhaseContext(
            firstWorkoutDate = null,
            referenceDate = LocalDate.of(2026, 1, 10)
        )
        coEvery { analyticsRepository.getRecentLogsForExercise(EXERCISE_ID) } returns emptyList()
        coEvery { sendArchitectAnalysis.invoke(any()) } throws IllegalStateException("RAW_AI_503")
        coEvery { decideTodayWorkout.invoke(any()) } throws IllegalStateException("No Today Order")
        every { validateDirectives.invoke(any(), matrix, any()) } answers {
            Result.Success(
                DirectiveValidationResult(
                    validatedDirectives = firstArg<List<WorkoutDirective>>(),
                    audits = emptyList()
                )
            )
        }

        val result = useCase().invoke(
            session = WorkoutSession(
                questId = 0L,
                timestamp = clock.now(),
                totalTonnage = 0.0,
                cycleDay = 1
            ),
            sets = sets,
            plannedRecommendations = listOf(
                SetRecommendation(
                    weight = 100.0,
                    reps = 8,
                    sets = 3,
                    isProgression = false,
                    exerciseId = EXERCISE_ID
                )
            )
        )

        assertTrue(result is Result.Success)
        val report = (result as Result.Success).data
        assertTrue(report.isFallback)
        assertEquals("[ SYSTEM_VERDICT ]", report.currentStageStatus)
        assertEquals(MessageTextKey.AI_FALLBACK_ACTIVATED, (report.architectFeedback as MessageText.Resource).key)
        assertEquals(SESSION_ID, report.sessionId)
        assertNotNull(report.judgment)
        assertEquals(WorkoutProgressionDecision.INCREASE_ALLOWED, report.judgment?.progressionDecision)
        assertEquals(EXERCISE_ID, report.nextWorkoutDirectives.single().exerciseId)
        coVerify(exactly = 0) {
            progressionMatrixRepository.updateTarget(any(), any(), any(), any(), any(), any())
        }
    }

    private fun useCase(): FinalizeSessionUseCase =
        FinalizeSessionUseCase(
            analyticsRepository = analyticsRepository,
            sendArchitectAnalysis = sendArchitectAnalysis,
            progressionMatrixRepository = progressionMatrixRepository,
            recalculateGlobalRank = recalculateGlobalRank,
            calculateRecovery = CalculateRecoveryWindowUseCase(),
            validateDirectives = validateDirectives,
            transactionProvider = transactionProvider,
            getWeightContext = getWeightContext,
            getTrainingPhaseContext = getTrainingPhaseContext,
            questRepository = questRepository,
            completeQuest = completeQuest,
            calculateProgressRank = calculateProgressRank,
            calculateWorkoutJudgment = calculateWorkoutJudgment,
            decideTodayWorkout = decideTodayWorkout,
            clock = clock,
            logger = logger
        )

    private fun matrixEntry(): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = EXERCISE_ID,
            exerciseName = "Bench Press",
            startWeight = 80f,
            targetWeight = 140f,
            currentWeight = 100f,
            targetWeightNote = null,
            weeklyStep = 2.5f,
            progressPercent = 30f,
            currentRank = Rank.E,
            nextRecommendedSets = 3,
            nextRecommendedReps = "8"
        )

    private fun completedSet(): ExerciseSet =
        ExerciseSet(
            sessionId = 0L,
            exerciseId = EXERCISE_ID,
            weight = 100.0,
            reps = 8,
            isCompleted = true
        )

    private class FixedClock(
        private val today: LocalDate
    ) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(zoneId()).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }

    private companion object {
        const val EXERCISE_ID = 42
        const val SESSION_ID = 99L
    }
}
