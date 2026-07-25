package com.ihor.thesystem.feature.status.ui.components.workout

import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel

internal data class WorkoutSetGroup(
    val weight: String,
    val sets: List<ActiveSetInput>
)

internal fun List<ActiveSetInput>.groupAdjacentByWeight(): List<WorkoutSetGroup> {
    if (isEmpty()) return emptyList()
    val groups = mutableListOf<WorkoutSetGroup>()
    var currentWeight = first().weight
    val currentSets = mutableListOf<ActiveSetInput>()

    forEach { set ->
        if (currentSets.isNotEmpty() && set.weight != currentWeight) {
            groups += WorkoutSetGroup(currentWeight, currentSets.toList())
            currentSets.clear()
            currentWeight = set.weight
        }
        currentSets += set
    }

    if (currentSets.isNotEmpty()) {
        groups += WorkoutSetGroup(currentWeight, currentSets.toList())
    }
    return groups
}

internal fun ExerciseWorkoutUiModel.techniqueCheckText(): String? {
    if (!isCoreSystemExercise) return null

    val tips = techniqueTips
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(3)

    if (tips.isEmpty()) return null

    return "Перевірте техніку: ${tips.joinToString(" · ")}"
}

internal fun ActiveSetInput.isReadyForCompletion(
    weightValue: String,
    repsValue: String,
    trackingMode: ExerciseTrackingMode
): Boolean {
    val hasValidLoad = !trackingMode.usesWeightInput || weightValue.toPositiveDoubleOrNull() != null
    return hasValidLoad && repsValue.trim().isNotEmpty()
}

private fun String.toPositiveDoubleOrNull(): Double? =
    replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }

internal fun ExerciseTrackingMode.shortValueHint(): String =
    when (this) {
        ExerciseTrackingMode.WEIGHT_REPS,
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "повт."
        ExerciseTrackingMode.TIME_SECONDS -> "сек"
        ExerciseTrackingMode.TIME_MINUTES -> "хв"
    }

internal fun ExerciseTrackingMode.loadSlotLabel(): String =
    when (this) {
        ExerciseTrackingMode.WEIGHT_REPS -> "кг"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "без ваги"
        ExerciseTrackingMode.TIME_SECONDS,
        ExerciseTrackingMode.TIME_MINUTES -> "час"
    }

internal fun ExerciseTrackingMode.inputSummary(setCount: Int): String =
    when (this) {
        ExerciseTrackingMode.WEIGHT_REPS -> "$setCount підх. · вага + повтори"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "$setCount підх. · повтори"
        ExerciseTrackingMode.TIME_SECONDS -> "$setCount підх. · секунди"
        ExerciseTrackingMode.TIME_MINUTES -> "$setCount підх. · хвилини"
    }
