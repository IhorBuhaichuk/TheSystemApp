package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import java.util.Locale

internal fun QuestTask.toSyntheticExerciseDetails(): ExerciseDetails =
    ExerciseDetails(
        id = exerciseId ?: -id.coerceAtLeast(1),
        name = name,
        nameUk = nameUk,
        category = ExerciseCategory.FLEXIBILITY,
        equipment = SYSTEM_BODYWEIGHT_EQUIPMENT,
        trackingMode = inferSystemTrackingMode(name)
    )

private fun inferSystemTrackingMode(name: String): String {
    val normalized = name.lowercase()
    return when {
        normalized.contains("walking") || normalized.contains("walk") ->
            ExerciseTrackingMode.TIME_MINUTES.name
        normalized.contains("hold") ||
            normalized.contains("plank") ||
            normalized.contains("mobility") ||
            normalized.contains("stretch") ->
            ExerciseTrackingMode.TIME_SECONDS.name
        else -> ExerciseTrackingMode.BODYWEIGHT_REPS.name
    }
}

internal fun formatRecommendation(
    weight: Double,
    reps: Int,
    sets: Int,
    trackingMode: ExerciseTrackingMode
): String =
    when (trackingMode) {
        ExerciseTrackingMode.WEIGHT_REPS -> "$sets × $reps · ${weight.formatInputWeight()} кг"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "$sets × $reps повт."
        ExerciseTrackingMode.TIME_SECONDS -> "$sets × $reps сек"
        ExerciseTrackingMode.TIME_MINUTES -> "$sets × ${(reps / 60).coerceAtLeast(1)} хв"
    }

internal fun TodayTrainingDecision.toWorkoutAdjustmentReason(): String? =
    when (decisionType) {
        TodayTrainingDecisionType.REDUCED_LOAD,
        TodayTrainingDecisionType.ACTIVE_RECOVERY,
        TodayTrainingDecisionType.DELOAD ->
            "Готовність — $readinessScore%. Через накопичену втому навантаження зменшено."
        TodayTrainingDecisionType.NO_EXCUSE ->
            if (reason.contains("missed", ignoreCase = true)) {
                "Система зафіксувала пропуск. План перераховано. Наступна оптимальна дія: коротке тренування."
            } else {
                "Готовність нижча за планову. Наступна оптимальна дія: коротке тренування."
            }
        else -> null
    }

internal fun ExerciseTrackingMode.formatTargetInput(targetReps: Int): String =
    when (this) {
        ExerciseTrackingMode.TIME_MINUTES -> fromStoredTimeSeconds(targetReps).coerceAtLeast(1).toString()
        else -> targetReps.coerceAtLeast(1).toString()
    }

internal fun Double.formatInputWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }

internal fun Float.formatEquipmentNumber(): String =
    if (this % 1f == 0f) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }

private const val SYSTEM_BODYWEIGHT_EQUIPMENT = "body only"

internal val GYM_DEFAULT_EQUIPMENT = setOf(
    EquipmentType.BODY_ONLY,
    EquipmentType.DUMBBELL,
    EquipmentType.BARBELL,
    EquipmentType.BENCH,
    EquipmentType.PULL_UP_BAR,
    EquipmentType.DIP_BARS,
    EquipmentType.BANDS,
    EquipmentType.MACHINE,
    EquipmentType.CABLE
)
