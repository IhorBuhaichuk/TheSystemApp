package com.ihor.thesystem.feature.exercise_search.ui

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ExerciseCategory

fun ExerciseCategory.toUiText(): UiText {
    return when (this) {
        ExerciseCategory.STRENGTH -> UiText.StringResource(R.string.filter_category_strength)
        ExerciseCategory.ENDURANCE -> UiText.StringResource(R.string.filter_category_endurance)
        ExerciseCategory.HYPERTROPHY -> UiText.StringResource(R.string.filter_category_hypertrophy)
        ExerciseCategory.FLEXIBILITY -> UiText.StringResource(R.string.filter_category_flexibility)
        ExerciseCategory.BALANCE -> UiText.StringResource(R.string.filter_category_balance)
        ExerciseCategory.UNKNOWN -> UiText.DynamicString(this.name)
    }
}

fun String.toMuscleUiText(): UiText {
    return when (this.uppercase()) {
        "CHEST" -> UiText.StringResource(R.string.filter_muscle_chest)
        "BACK" -> UiText.StringResource(R.string.filter_muscle_back)
        "SHOULDERS" -> UiText.StringResource(R.string.filter_muscle_shoulders)
        "QUADS" -> UiText.StringResource(R.string.filter_muscle_quads)
        "HAMSTRINGS_GLUTES" -> UiText.StringResource(R.string.filter_muscle_hamstrings_glutes)
        "ARMS" -> UiText.StringResource(R.string.filter_muscle_arms)
        "ABS" -> UiText.StringResource(R.string.filter_muscle_abs)
        "LEGS" -> UiText.StringResource(R.string.filter_muscle_legs)
        "CORE" -> UiText.StringResource(R.string.filter_muscle_core)
        else -> UiText.DynamicString(this)
    }
}

fun String.toEquipmentUiText(): UiText {
    return when (this.lowercase()) {
        "body only" -> UiText.StringResource(R.string.filter_equipment_body_only)
        "dumbbell" -> UiText.StringResource(R.string.filter_equipment_dumbbell)
        "barbell" -> UiText.StringResource(R.string.filter_equipment_barbell)
        "cable" -> UiText.StringResource(R.string.filter_equipment_cable)
        "machine" -> UiText.StringResource(R.string.filter_equipment_machine)
        "kettlebells" -> UiText.StringResource(R.string.filter_equipment_kettlebells)
        "bands" -> UiText.StringResource(R.string.filter_equipment_bands)
        else -> UiText.DynamicString(this)
    }
}

fun String.toLevelUiText(): UiText {
    return when (this.lowercase()) {
        "beginner" -> UiText.StringResource(R.string.filter_level_beginner)
        "intermediate" -> UiText.StringResource(R.string.filter_level_intermediate)
        "expert" -> UiText.StringResource(R.string.filter_level_expert)
        else -> UiText.DynamicString(this)
    }
}

fun String.toMechanicUiText(): UiText {
    return when (this.lowercase()) {
        "compound" -> UiText.StringResource(R.string.filter_mechanic_compound)
        "isolation" -> UiText.StringResource(R.string.filter_mechanic_isolation)
        else -> UiText.DynamicString(this)
    }
}

fun String.toForceUiText(): UiText {
    return when (this.lowercase()) {
        "pull" -> UiText.StringResource(R.string.filter_force_pull)
        "push" -> UiText.StringResource(R.string.filter_force_push)
        "static" -> UiText.StringResource(R.string.filter_force_static)
        else -> UiText.DynamicString(this)
    }
}
