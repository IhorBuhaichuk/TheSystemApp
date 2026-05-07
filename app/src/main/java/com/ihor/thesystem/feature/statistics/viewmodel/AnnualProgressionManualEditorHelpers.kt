package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.domain.model.ExerciseCategory

internal data class ScheduledExerciseSeed(
    val exerciseId: Int,
    val exerciseName: String,
    val exerciseNameUk: String?,
    val category: ExerciseCategory,
    val equipment: String?,
    val externalId: String?,
    val trackingMode: String?,
    val cycleDays: MutableList<Int>
)

internal fun String.normalizedDouble(): Double? =
    replace(',', '.').toDoubleOrNull()

internal const val DEFAULT_CYCLE_DAYS = 4
internal const val DEFAULT_INVENTORY_STEP = 2.5
internal const val MANUAL_MONTH_COUNT = 12
internal const val FORCED_JUMP_THRESHOLD = 1.25
internal const val WEIGHT_EPSILON = 0.001
