package com.ihor.thesystem.domain.model

import kotlin.math.roundToInt

enum class ExerciseTrackingMode {
    WEIGHT_REPS,
    BODYWEIGHT_REPS,
    TIME_SECONDS,
    TIME_MINUTES;

    val usesWeightInput: Boolean
        get() = this == WEIGHT_REPS

    val usesTimeInput: Boolean
        get() = this == TIME_SECONDS || this == TIME_MINUTES

    val valueLabel: String
        get() = when (this) {
            WEIGHT_REPS,
            BODYWEIGHT_REPS -> "Повт."
            TIME_SECONDS -> "Сек."
            TIME_MINUTES -> "Хв."
        }

    val valueHint: String
        get() = when (this) {
            WEIGHT_REPS,
            BODYWEIGHT_REPS -> "reps"
            TIME_SECONDS -> "сек"
            TIME_MINUTES -> "хв"
        }

    val metricUnit: String
        get() = when (this) {
            WEIGHT_REPS -> "кг"
            BODYWEIGHT_REPS -> "повт."
            TIME_SECONDS -> "сек"
            TIME_MINUTES -> "хв"
        }

    fun toStoredTimeSeconds(value: Int): Int =
        when (this) {
            TIME_MINUTES -> value * SECONDS_IN_MINUTE
            else -> value
        }

    fun fromStoredTimeSeconds(seconds: Int): Int =
        when (this) {
            TIME_MINUTES -> seconds / SECONDS_IN_MINUTE
            else -> seconds
        }
}

object ExerciseTrackingModeResolver {
    fun resolve(
        exercise: ExerciseDetails,
        reference: ReferenceMatrix? = null
    ): ExerciseTrackingMode =
        exercise.trackingMode.toExerciseTrackingModeOrNull()
            ?: resolve(
                name = exercise.name,
                nameUk = exercise.nameUk,
                externalId = exercise.externalId,
                category = exercise.category,
                equipment = exercise.equipment,
                referenceWeightType = reference?.weightType
            )

    fun resolve(
        trackingModeOverride: String?,
        name: String,
        nameUk: String? = null,
        externalId: String? = null,
        category: ExerciseCategory = ExerciseCategory.UNKNOWN,
        equipment: String? = null,
        referenceWeightType: ExerciseWeightType? = null
    ): ExerciseTrackingMode =
        trackingModeOverride.toExerciseTrackingModeOrNull() ?:
        resolve(
            name = name,
            nameUk = nameUk,
            externalId = externalId,
            category = category,
            equipment = equipment,
            referenceWeightType = referenceWeightType
        )

    fun resolve(
        name: String,
        nameUk: String? = null,
        externalId: String? = null,
        category: ExerciseCategory = ExerciseCategory.UNKNOWN,
        equipment: String? = null,
        referenceWeightType: ExerciseWeightType? = null
    ): ExerciseTrackingMode {
        val normalized = listOfNotNull(name, nameUk, externalId)
            .joinToString(separator = " ")
            .lowercase()

        if (normalized.containsAny(TIME_SECONDS_KEYWORDS)) {
            return ExerciseTrackingMode.TIME_SECONDS
        }
        if (category == ExerciseCategory.ENDURANCE && normalized.containsAny(TIME_MINUTES_KEYWORDS)) {
            return ExerciseTrackingMode.TIME_MINUTES
        }
        if (normalized.containsAny(TIME_MINUTES_KEYWORDS)) {
            return ExerciseTrackingMode.TIME_MINUTES
        }
        val normalizedEquipment = equipment.orEmpty().lowercase()
        val isBodyOnlyEquipment = normalizedEquipment == BODY_ONLY_EQUIPMENT ||
            (normalizedEquipment.contains("body") && normalizedEquipment.contains("only")) ||
            (normalizedEquipment.contains("body") && normalizedEquipment.contains("weight"))
        val isNamedBodyweight = normalized.containsAny(BODYWEIGHT_KEYWORDS) &&
            !normalized.containsAny(EXTERNAL_LOAD_KEYWORDS)

        if (referenceWeightType == ExerciseWeightType.BODY_WEIGHT || isBodyOnlyEquipment || isNamedBodyweight) {
            return ExerciseTrackingMode.BODYWEIGHT_REPS
        }
        return ExerciseTrackingMode.WEIGHT_REPS
    }

    private fun String.containsAny(keywords: List<String>): Boolean =
        keywords.any(::contains)
}

fun ExerciseTrackingMode.formatPrimaryValue(value: Double): String =
    when (this) {
        ExerciseTrackingMode.WEIGHT_REPS -> "${value.formatMetricNumber()} кг"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "${value.formatMetricNumber()} повт."
        ExerciseTrackingMode.TIME_SECONDS -> "${value.formatMetricNumber()} сек"
        ExerciseTrackingMode.TIME_MINUTES -> "${value.formatMetricNumber()} хв"
    }

fun String?.toExerciseTrackingModeOrNull(): ExerciseTrackingMode? =
    this?.let { value ->
        runCatching { ExerciseTrackingMode.valueOf(value) }.getOrNull()
    }

fun ActiveSetInput.toExerciseSetOrNull(
    exerciseId: Int,
    trackingMode: ExerciseTrackingMode,
    sessionId: Long = 0L,
    userFeedback: String? = null
): ExerciseSet? {
    val storedWeight = if (trackingMode.usesWeightInput) {
        weight.normalizedPositiveDoubleOrNull() ?: return null
    } else {
        TECHNICAL_BODYWEIGHT_LOAD
    }

    val storedReps = if (trackingMode.usesTimeInput) {
        reps.toStoredDurationSeconds(trackingMode) ?: return null
    } else {
        reps.toIntOrNull()?.takeIf { it > 0 } ?: return null
    }

    return ExerciseSet(
        sessionId = sessionId,
        exerciseId = exerciseId,
        weight = storedWeight,
        reps = storedReps,
        isCompleted = isCompleted,
        userFeedback = userFeedback
    )
}

fun ExerciseSet.toActiveSetInput(trackingMode: ExerciseTrackingMode): ActiveSetInput =
    ActiveSetInput(
        weight = if (trackingMode.usesWeightInput && weight > 0.0) weight.formatMetricNumber() else "",
        reps = when {
            trackingMode.usesTimeInput && reps > 0 -> trackingMode.formatStoredDurationInput(reps)
            reps > 0 -> reps.toString()
            else -> ""
        },
        isCompleted = isCompleted
    )

fun ActiveSetInput.toStoredActiveSetInputOrNull(trackingMode: ExerciseTrackingMode): ActiveSetInput? {
    val parsed = toExerciseSetOrNull(
        exerciseId = 0,
        trackingMode = trackingMode
    ) ?: return null
    return copy(
        weight = parsed.weight.formatMetricNumber(),
        reps = parsed.reps.toString()
    )
}

fun ExerciseSet.formatForTrackingMode(trackingMode: ExerciseTrackingMode): String =
    when (trackingMode) {
        ExerciseTrackingMode.WEIGHT_REPS -> "${weight.formatMetricNumber()} кг x $reps"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "$reps повт."
        ExerciseTrackingMode.TIME_SECONDS -> "${reps} сек"
        ExerciseTrackingMode.TIME_MINUTES -> trackingMode.formatStoredDurationInput(reps) + " хв"
    }

private fun Double.formatMetricNumber(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", this)
    }

private fun String.normalizedPositiveDoubleOrNull(): Double? =
    replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }

private fun String.toStoredDurationSeconds(mode: ExerciseTrackingMode): Int? {
    val trimmed = trim()
    if (trimmed.isBlank()) return null
    val colonParts = trimmed.split(":")
    if (colonParts.size == 2) {
        val minutes = colonParts[0].toIntOrNull()?.takeIf { it >= 0 } ?: return null
        val seconds = colonParts[1].toIntOrNull()?.takeIf { it in 0..59 } ?: return null
        return (minutes * SECONDS_IN_MINUTE + seconds).takeIf { it > 0 }
    }
    val numericValue = trimmed.normalizedPositiveDoubleOrNull() ?: return null
    return when (mode) {
        ExerciseTrackingMode.TIME_MINUTES -> (numericValue * SECONDS_IN_MINUTE).roundToInt()
        else -> numericValue.roundToInt()
    }.takeIf { it > 0 }
}

private fun ExerciseTrackingMode.formatStoredDurationInput(seconds: Int): String =
    when (this) {
        ExerciseTrackingMode.TIME_MINUTES -> {
            val minutesValue = seconds.toDouble() / SECONDS_IN_MINUTE
            minutesValue.formatMetricNumber()
        }
        else -> seconds.toString()
    }

private const val BODY_ONLY_EQUIPMENT = "body only"
private const val TECHNICAL_BODYWEIGHT_LOAD = 1.0
private const val SECONDS_IN_MINUTE = 60

private val TIME_SECONDS_KEYWORDS = listOf(
    "plank",
    "планка",
    "планк",
    "wall sit",
    "hold",
    "утрим"
)

private val TIME_MINUTES_KEYWORDS = listOf(
    "run",
    "running",
    "jog",
    "bicycl",
    "bike",
    "cycling",
    "elliptical",
    "row",
    "rowing",
    "skating",
    "walking",
    "walk",
    "rope jumping",
    "jump rope",
    "treadmill",
    "біг",
    "пробіж",
    "ходь",
    "велосип",
    "греб",
    "скакал"
)

private val BODYWEIGHT_KEYWORDS = listOf(
    "push-up",
    "pushup",
    "pull-up",
    "pullup",
    "chin-up",
    "chinup",
    "dip",
    "віджим",
    "підтяг",
    "брус",
    "бурпі",
    "burpee"
)

private val EXTERNAL_LOAD_KEYWORDS = listOf(
    "weighted",
    "barbell",
    "dumbbell",
    "kettlebell",
    "гантел",
    "штанг",
    "гир",
    "з вагою",
    "з обтяж"
)
