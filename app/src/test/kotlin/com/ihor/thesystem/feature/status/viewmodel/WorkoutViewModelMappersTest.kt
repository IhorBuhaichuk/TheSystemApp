package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutViewModelMappersTest {

    @Test
    fun `formats recommendations by tracking mode`() {
        assertEquals(
            "3x8 @ 42.5kg",
            formatRecommendation(42.5, reps = 8, sets = 3, trackingMode = ExerciseTrackingMode.WEIGHT_REPS)
        )
        assertEquals(
            "3x12 повт.",
            formatRecommendation(0.0, reps = 12, sets = 3, trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS)
        )
        assertEquals(
            "2x45 сек",
            formatRecommendation(0.0, reps = 45, sets = 2, trackingMode = ExerciseTrackingMode.TIME_SECONDS)
        )
        assertEquals(
            "1x2 хв",
            formatRecommendation(0.0, reps = 120, sets = 1, trackingMode = ExerciseTrackingMode.TIME_MINUTES)
        )
    }

    @Test
    fun `maps quest task without exercise id to synthetic bodyweight exercise`() {
        val exercise = QuestTask(
            id = 7,
            questId = 2,
            name = "Plank hold",
            nameUk = "Планка",
            isCompleted = false
        ).toSyntheticExerciseDetails()

        assertEquals(-7, exercise.id)
        assertEquals("body only", exercise.equipment)
        assertEquals(ExerciseTrackingMode.TIME_SECONDS.name, exercise.trackingMode)
    }

    @Test
    fun `maps workout adjustment reason only for blocking decisions`() {
        assertEquals(
            "Система знизила навантаження через readiness 62% і recovery debt HIGH.",
            decision(TodayTrainingDecisionType.REDUCED_LOAD).toWorkoutAdjustmentReason()
        )
        assertEquals(
            "Система зафіксувала пропуск. План перераховано. Наступна оптимальна дія: коротке тренування.",
            decision(TodayTrainingDecisionType.NO_EXCUSE, reason = "missed workout").toWorkoutAdjustmentReason()
        )
        assertNull(decision(TodayTrainingDecisionType.STANDARD_TRAINING).toWorkoutAdjustmentReason())
    }

    @Test
    fun `formats active input defaults`() {
        assertEquals("42", 42.0.formatInputWeight())
        assertEquals("42.5", 42.5.formatInputWeight())
        assertEquals("2", ExerciseTrackingMode.TIME_MINUTES.formatTargetInput(120))
        assertEquals("1", ExerciseTrackingMode.TIME_MINUTES.formatTargetInput(0))
        assertEquals("24", 24f.formatEquipmentNumber())
        assertEquals("24.5", 24.5f.formatEquipmentNumber())
    }

    private fun decision(
        type: TodayTrainingDecisionType,
        reason: String = "readiness"
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = 0L,
            cycleDay = 1,
            workoutName = "A",
            readinessScore = 62,
            readinessLevel = ReadinessLevel.REDUCED,
            recoveryDebt = RecoveryDebt(
                value = 70,
                level = RecoveryDebtLevel.HIGH,
                reasons = listOf("volume")
            ),
            decisionType = type,
            loadMultiplier = 1f,
            volumeMultiplier = 1f,
            reason = reason,
            warnings = emptyList(),
            selectedWorkoutTemplateId = 1,
            isTrainingAllowed = type != TodayTrainingDecisionType.REST
        )
}
