package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustWorkoutRecommendationUseCaseTest {

    private val useCase = AdjustWorkoutRecommendationUseCase()

    @Test
    fun `reduced load scales weight and volume with minimum sets`() {
        val adjusted = useCase(
            recommendation = recommendation(weight = 100.0, sets = 3, reps = 10),
            trackingMode = ExerciseTrackingMode.WEIGHT_REPS,
            decision = decision(
                type = TodayTrainingDecisionType.REDUCED_LOAD,
                loadMultiplier = 0.85f,
                volumeMultiplier = 0.7f
            )
        )

        assertEquals(100.0, adjusted.baseWeight, 0.001)
        assertEquals(85.0, adjusted.adjustedWeight, 0.001)
        assertEquals(3, adjusted.baseSets)
        assertEquals(2, adjusted.adjustedSets)
        assertEquals(10, adjusted.adjustedReps)
    }

    @Test
    fun `deload reduces load and volume`() {
        val adjusted = useCase(
            recommendation = recommendation(weight = 100.0, sets = 4, reps = 8),
            trackingMode = ExerciseTrackingMode.WEIGHT_REPS,
            decision = decision(
                type = TodayTrainingDecisionType.DELOAD,
                loadMultiplier = 0.8f,
                volumeMultiplier = 0.6f,
                debtLevel = RecoveryDebtLevel.HIGH
            )
        )

        assertEquals(80.0, adjusted.adjustedWeight, 0.001)
        assertEquals(2, adjusted.adjustedSets)
        assertEquals(8, adjusted.adjustedReps)
        assertTrue(adjusted.adjustmentReason.contains("Deload"))
    }

    @Test
    fun `no excuse caps individual exercise to two sets`() {
        val adjusted = useCase(
            recommendation = recommendation(weight = 100.0, sets = 5, reps = 12),
            trackingMode = ExerciseTrackingMode.WEIGHT_REPS,
            decision = decision(type = TodayTrainingDecisionType.NO_EXCUSE)
        )

        assertEquals(90.0, adjusted.adjustedWeight, 0.001)
        assertEquals(2, adjusted.adjustedSets)
        assertEquals(12, adjusted.adjustedReps)
    }

    @Test
    fun `progress allowed keeps matrix capped progression recommendation`() {
        val adjusted = useCase(
            recommendation = recommendation(
                weight = 102.5,
                sets = 3,
                reps = 8,
                isProgression = true
            ),
            trackingMode = ExerciseTrackingMode.WEIGHT_REPS,
            decision = decision(
                type = TodayTrainingDecisionType.PROGRESS_ALLOWED,
                loadMultiplier = 1.025f
            )
        )

        assertEquals(102.5, adjusted.adjustedWeight, 0.001)
        assertEquals(3, adjusted.adjustedSets)
        assertEquals(8, adjusted.adjustedReps)
        assertTrue(adjusted.adjustmentReason.contains("matrix target"))
    }

    private fun recommendation(
        weight: Double,
        sets: Int,
        reps: Int,
        isProgression: Boolean = false
    ): SetRecommendation =
        SetRecommendation(
            exerciseId = EXERCISE_ID,
            weight = weight,
            sets = sets,
            reps = reps,
            isProgression = isProgression
        )

    private fun decision(
        type: TodayTrainingDecisionType,
        loadMultiplier: Float = 1f,
        volumeMultiplier: Float = 1f,
        debtLevel: RecoveryDebtLevel = RecoveryDebtLevel.LOW
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = 0L,
            cycleDay = 1,
            workoutName = "Test workout",
            readinessScore = 58,
            readinessLevel = ReadinessLevel.REDUCED,
            recoveryDebt = RecoveryDebt(
                value = 30,
                level = debtLevel,
                reasons = emptyList()
            ),
            decisionType = type,
            loadMultiplier = loadMultiplier,
            volumeMultiplier = volumeMultiplier,
            reason = "Test decision",
            warnings = emptyList(),
            selectedWorkoutTemplateId = 1,
            isTrainingAllowed = type != TodayTrainingDecisionType.REST
        )

    private companion object {
        const val EXERCISE_ID = 42
    }
}
