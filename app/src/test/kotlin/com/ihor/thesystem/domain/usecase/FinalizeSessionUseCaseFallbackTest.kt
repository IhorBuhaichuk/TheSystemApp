package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DirectiveValidationResult
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.model.MessageTextKey
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutProgressionDecision
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.repository.WorkoutDraftRepository
import com.ihor.thesystem.domain.repository.WorkoutSessionCommit
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
    private val workoutDraftRepository: WorkoutDraftRepository = mockk(relaxed = true)
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
        coEvery {
            analyticsRepository.commitSessionOnce(COMPLETION_ID, any(), any())
        } returns WorkoutSessionCommit(SESSION_ID, wasCreated = true)
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
            completionId = COMPLETION_ID,
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

    @Test
    fun `retry returns same session and runs canonical side effects once`() = runTest {
        val matrix = listOf(matrixEntry())
        val sets = listOf(completedSet())
        val quest = Quest(
            id = QUEST_ID,
            title = "Workout A",
            type = DomainQuestType.MAIN,
            date = clock.now(),
            status = DomainQuestStatus.ACTIVE,
            tasks = listOf(
                QuestTask(
                    id = 1,
                    questId = QUEST_ID,
                    name = "Bench Press",
                    isCompleted = false,
                    exerciseId = EXERCISE_ID
                )
            ),
            scheduleId = 1
        )
        coEvery { transactionProvider.runInTransaction<Any?>(any()) } coAnswers {
            firstArg<suspend () -> Any?>().invoke()
        }
        coEvery {
            analyticsRepository.commitSessionOnce(COMPLETION_ID, any(), any())
        } returnsMany listOf(
            WorkoutSessionCommit(SESSION_ID, wasCreated = true),
            WorkoutSessionCommit(SESSION_ID, wasCreated = false)
        )
        coEvery { questRepository.getQuestById(QUEST_ID) } returns quest
        coEvery { questRepository.completeQuestTasksForExercises(QUEST_ID, any()) } returns Unit
        coEvery { getWeightContext.invoke() } returns PlayerWeightContext(null, null)
        every { progressionMatrixRepository.getAllEntries() } returns flowOf(matrix)
        every { calculateProgressRank.invoke(any(), any(), any()) } returns null
        coEvery { recalculateGlobalRank.invoke() } returns Unit
        coEvery {
            completeQuest.invoke(QUEST_ID, QuestCompletionMode.TaskUpdate, false)
        } returns Result.Success(
            QuestCompletionOutcome(
                questId = QUEST_ID,
                status = DomainQuestStatus.COMPLETED,
                rewardGranted = true,
                logWritten = true,
                penaltyActivated = false
            )
        )
        coEvery { getTrainingPhaseContext.invoke(any()) } returns TrainingPhaseContext(
            firstWorkoutDate = null,
            referenceDate = LocalDate.of(2026, 1, 10)
        )
        coEvery { analyticsRepository.getRecentLogsForExercise(EXERCISE_ID) } returns emptyList()
        coEvery { sendArchitectAnalysis.invoke(any()) } throws IllegalStateException("offline")
        coEvery { decideTodayWorkout.invoke(any()) } throws IllegalStateException("No Today Order")

        val session = WorkoutSession(
            questId = QUEST_ID.toLong(),
            timestamp = clock.now(),
            totalTonnage = 800.0,
            cycleDay = 1
        )
        val first = useCase().invoke(COMPLETION_ID, session, sets)
        val retry = useCase().invoke(COMPLETION_ID, session, sets)

        assertTrue(first is Result.Success)
        assertTrue(retry is Result.Success)
        assertEquals(SESSION_ID, (first as Result.Success).data.sessionId)
        assertEquals(SESSION_ID, (retry as Result.Success).data.sessionId)
        coVerify(exactly = 2) { workoutDraftRepository.discardDraft(COMPLETION_ID) }
        coVerify(exactly = 1) { recalculateGlobalRank.invoke() }
        coVerify(exactly = 1) {
            completeQuest.invoke(QUEST_ID, QuestCompletionMode.TaskUpdate, false)
        }
        coVerify(exactly = 1) {
            questRepository.completeQuestTasksForExercises(any(), any())
        }
    }

    @Test
    fun `directive persistence failure after commit returns local report`() = runTest {
        val matrix = listOf(matrixEntry())
        val sets = listOf(completedSet())
        coEvery { transactionProvider.runInTransaction<Any?>(any()) } coAnswers {
            firstArg<suspend () -> Any?>().invoke()
        }
        coEvery {
            analyticsRepository.commitSessionOnce(COMPLETION_ID, any(), any())
        } returns WorkoutSessionCommit(SESSION_ID, wasCreated = true)
        coEvery { getWeightContext.invoke() } returns PlayerWeightContext(null, null)
        every { progressionMatrixRepository.getAllEntries() } returns flowOf(matrix)
        every { calculateProgressRank.invoke(any(), any(), any()) } returns null
        coEvery { recalculateGlobalRank.invoke() } returns Unit
        coEvery { getTrainingPhaseContext.invoke(any()) } returns TrainingPhaseContext(
            firstWorkoutDate = null,
            referenceDate = LocalDate.of(2026, 1, 10)
        )
        coEvery { analyticsRepository.getRecentLogsForExercise(EXERCISE_ID) } returns emptyList()
        coEvery { decideTodayWorkout.invoke(any()) } throws IllegalStateException("No Today Order")
        coEvery { sendArchitectAnalysis.invoke(any()) } returns ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("AI report"),
            recommendations = listOf(
                AiWorkoutRecommendation(EXERCISE_ID, 102.5f, 3, "8")
            )
        )
        every { validateDirectives.invoke(any(), matrix, any()) } answers {
            Result.Success(
                DirectiveValidationResult(
                    validatedDirectives = firstArg<List<WorkoutDirective>>(),
                    audits = emptyList()
                )
            )
        }
        coEvery { analyticsRepository.saveDirectives(any()) } throws IllegalStateException("disk full")

        val result = useCase().invoke(
            completionId = COMPLETION_ID,
            session = WorkoutSession(
                questId = 0L,
                timestamp = clock.now(),
                totalTonnage = 800.0,
                cycleDay = 1
            ),
            sets = sets
        )

        assertTrue(result is Result.Success)
        val report = (result as Result.Success).data
        assertTrue(report.isFallback)
        assertEquals(SESSION_ID, report.sessionId)
        coVerify(exactly = 1) {
            analyticsRepository.commitSessionOnce(COMPLETION_ID, any(), any())
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
            workoutDraftRepository = workoutDraftRepository,
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
        const val COMPLETION_ID = "draft-completion-1"
        const val QUEST_ID = 17
    }
}
