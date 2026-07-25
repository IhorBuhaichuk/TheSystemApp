package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutViewModelMappersTest {

    @Test
    fun `formats recommendations by tracking mode`() {
        assertEquals(
            "3 × 8 · 42.5 кг",
            formatRecommendation(42.5, reps = 8, sets = 3, trackingMode = ExerciseTrackingMode.WEIGHT_REPS)
        )
        assertEquals(
            "3 × 12 повт.",
            formatRecommendation(0.0, reps = 12, sets = 3, trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS)
        )
        assertEquals(
            "2 × 45 сек",
            formatRecommendation(0.0, reps = 45, sets = 2, trackingMode = ExerciseTrackingMode.TIME_SECONDS)
        )
        assertEquals(
            "1 × 2 хв",
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
            "Готовність — 62%. Через накопичену втому навантаження зменшено.",
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

    @Test
    fun `workout logging summary blocks finish before first completed set`() {
        val summary = buildWorkoutLoggingSummary(
            workout(
                exercise(
                    ActiveSetInput(weight = "80", reps = "8", isCompleted = false),
                    ActiveSetInput()
                )
            )
        )

        assertFalse(summary.canFinish)
        assertEquals(0, summary.completedSets)
        assertEquals(2, summary.totalSets)
        assertEquals("0/2 підх.", summary.progressText)
        assertEquals("Завершити тренування", summary.finishCtaText)
    }

    @Test
    fun `workout logging summary enables finish with partial logged workout`() {
        val summary = buildWorkoutLoggingSummary(
            workout(
                exercise(
                    ActiveSetInput(weight = "80", reps = "8", isCompleted = true),
                    ActiveSetInput(weight = "80", reps = "8", isCompleted = false)
                ),
                exercise(
                    ActiveSetInput(weight = "60", reps = "10", isCompleted = false)
                )
            )
        )

        assertTrue(summary.canFinish)
        assertEquals(1, summary.completedSets)
        assertEquals(3, summary.totalSets)
        assertEquals(1, summary.completedExercises)
        assertEquals(2, summary.totalExercises)
        assertEquals(2, summary.remainingSets)
        assertEquals("Завершити · 1/3 підх.", summary.finishCtaText)
    }

    @Test
    fun `workout logging summary marks fully logged plan`() {
        val summary = buildWorkoutLoggingSummary(
            workout(
                exercise(
                    ActiveSetInput(weight = "80", reps = "8", isCompleted = true),
                    ActiveSetInput(weight = "80", reps = "8", isCompleted = true)
                )
            )
        )

        assertTrue(summary.canFinish)
        assertEquals(0, summary.remainingSets)
        assertEquals("План закрито. Можна завершувати без зайвих кроків.", summary.helperText)
        assertEquals("1/1 вправ", summary.exerciseText)
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

    private fun workout(
        vararg exercises: ExerciseWorkoutUiModel
    ): ActiveDayUiModel =
        ActiveDayUiModel(
            dayNumber = 1,
            dailyTasks = persistentListOf(),
            workoutName = "Workout A",
            exercises = persistentListOf(*exercises)
        )

    private fun exercise(
        vararg sets: ActiveSetInput
    ): ExerciseWorkoutUiModel =
        ExerciseWorkoutUiModel(
            exerciseId = 42,
            name = "Bench Press",
            sets = persistentListOf(*sets)
        )
}
