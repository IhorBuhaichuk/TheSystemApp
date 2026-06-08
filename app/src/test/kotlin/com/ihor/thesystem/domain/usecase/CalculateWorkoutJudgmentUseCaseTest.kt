package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.SystemWorkoutGrade
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutPerformanceStatus
import com.ihor.thesystem.domain.model.WorkoutProgressionDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateWorkoutJudgmentUseCaseTest {

    private val useCase = CalculateWorkoutJudgmentUseCase()

    @Test
    fun `all planned sets completed with reserve allows strong grade`() {
        val result = useCase(
            plannedRecommendations = listOf(plan(sets = 3, weight = 100.0, reps = 8)),
            actualSets = listOf(
                set(weight = 100.0, reps = 8),
                set(weight = 100.0, reps = 8),
                set(weight = 100.0, reps = 8)
            ),
            todayDecision = readiness(score = 82, level = ReadinessLevel.STANDARD)
        )

        assertTrue(result.grade == SystemWorkoutGrade.S || result.grade == SystemWorkoutGrade.A)
        assertEquals(100, result.completionPercent)
        assertEquals(WorkoutProgressionDecision.INCREASE_ALLOWED, result.progressionDecision)
    }

    @Test
    fun `partial volume produces b or c grade and holds progression`() {
        val result = useCase(
            plannedRecommendations = listOf(plan(sets = 4, weight = 80.0, reps = 10)),
            actualSets = listOf(
                set(weight = 80.0, reps = 10),
                set(weight = 80.0, reps = 10),
                set(weight = 80.0, reps = 10)
            ),
            todayDecision = readiness(score = 72, level = ReadinessLevel.STANDARD)
        )

        assertTrue(result.grade == SystemWorkoutGrade.B || result.grade == SystemWorkoutGrade.C)
        assertEquals(75, result.completionPercent)
        assertEquals(WorkoutPerformanceStatus.PARTIAL, result.performanceStatus)
        assertEquals(WorkoutProgressionDecision.HOLD, result.progressionDecision)
    }

    @Test
    fun `failed top set prevents progression`() {
        val result = useCase(
            plannedRecommendations = listOf(plan(sets = 3, weight = 100.0, reps = 8)),
            actualSets = listOf(
                set(weight = 100.0, reps = 8),
                set(weight = 100.0, reps = 8),
                set(weight = 100.0, reps = 5, completed = false)
            ),
            todayDecision = readiness(score = 75, level = ReadinessLevel.STANDARD)
        )

        assertTrue(
            result.progressionDecision == WorkoutProgressionDecision.HOLD ||
                result.progressionDecision == WorkoutProgressionDecision.REDUCE
        )
    }

    @Test
    fun `low readiness and hard session recommends deload`() {
        val result = useCase(
            plannedRecommendations = listOf(plan(sets = 3, weight = 90.0, reps = 8)),
            actualSets = listOf(
                set(weight = 90.0, reps = 8, feedback = "RPE 9 hard"),
                set(weight = 90.0, reps = 8, feedback = "RPE 9 hard"),
                set(weight = 90.0, reps = 8, feedback = "RPE 9 hard")
            ),
            todayDecision = readiness(score = 58, level = ReadinessLevel.REDUCED)
        )

        assertEquals(WorkoutProgressionDecision.DELOAD_RECOMMENDED, result.progressionDecision)
    }

    private fun plan(
        sets: Int,
        weight: Double,
        reps: Int
    ): SetRecommendation =
        SetRecommendation(
            weight = weight,
            reps = reps,
            sets = sets,
            isProgression = false,
            exerciseId = EXERCISE_ID
        )

    private fun set(
        weight: Double,
        reps: Int,
        completed: Boolean = true,
        feedback: String? = null
    ): ExerciseSet =
        ExerciseSet(
            sessionId = 1,
            exerciseId = EXERCISE_ID,
            weight = weight,
            reps = reps,
            isCompleted = completed,
            userFeedback = feedback
        )

    private fun readiness(
        score: Int,
        level: ReadinessLevel
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = 1,
            cycleDay = 1,
            workoutName = "Test",
            readinessScore = score,
            readinessLevel = level,
            recoveryDebt = RecoveryDebt(
                value = if (level == ReadinessLevel.REDUCED) 70 else 10,
                level = if (level == ReadinessLevel.REDUCED) RecoveryDebtLevel.HIGH else RecoveryDebtLevel.LOW,
                reasons = emptyList()
            ),
            decisionType = if (level == ReadinessLevel.REDUCED) {
                TodayTrainingDecisionType.REDUCED_LOAD
            } else {
                TodayTrainingDecisionType.STANDARD_TRAINING
            },
            loadMultiplier = 1f,
            volumeMultiplier = 1f,
            reason = "test",
            warnings = emptyList(),
            selectedWorkoutTemplateId = 1,
            isTrainingAllowed = true
        )

    private companion object {
        const val EXERCISE_ID = 42
    }
}
