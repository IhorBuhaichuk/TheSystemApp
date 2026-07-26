package com.ihor.thesystem.feature.exercise_search.ui

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MuscleGroup

fun ExerciseCategory.toUiText(): UiText {
    return when (this) {
        ExerciseCategory.STRENGTH -> UiText.StringResource(R.string.filter_category_strength)
        ExerciseCategory.ENDURANCE -> UiText.StringResource(R.string.filter_category_endurance)
        ExerciseCategory.HYPERTROPHY -> UiText.StringResource(R.string.filter_category_hypertrophy)
        ExerciseCategory.FLEXIBILITY -> UiText.StringResource(R.string.filter_category_flexibility)
        ExerciseCategory.BALANCE -> UiText.StringResource(R.string.filter_category_balance)
        ExerciseCategory.UNKNOWN -> UiText.StringResource(R.string.filter_category_other)
    }
}

fun MuscleGroup.toUiText(): UiText = this.name.toMuscleUiText()

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
        else -> UiText.StringResource(R.string.filter_muscle_other)
    }
}

fun String.toEquipmentUiText(): UiText {
    return when (this.lowercase()) {
        "body only" -> UiText.StringResource(R.string.filter_equipment_body_only)
        "machine" -> UiText.StringResource(R.string.filter_equipment_machine)
        "dumbbell" -> UiText.StringResource(R.string.filter_equipment_dumbbell)
        "barbell" -> UiText.StringResource(R.string.filter_equipment_barbell)
        "cable" -> UiText.StringResource(R.string.filter_equipment_cable)
        "bands" -> UiText.StringResource(R.string.filter_equipment_bands)
        "kettlebell", "kettlebells" -> UiText.StringResource(R.string.filter_equipment_kettlebell)
        "medicine ball" -> UiText.StringResource(R.string.filter_equipment_medicine_ball)
        "exercise ball" -> UiText.StringResource(R.string.filter_equipment_exercise_ball)
        "e-z curl bar" -> UiText.StringResource(R.string.filter_equipment_e_z_curl_bar)
        "foam roll" -> UiText.StringResource(R.string.filter_equipment_foam_roll)
        "other" -> UiText.StringResource(R.string.filter_equipment_other)
        else -> UiText.StringResource(R.string.filter_equipment_other)
    }
}

fun String.toLevelUiText(): UiText {
    return when (this.lowercase()) {
        "beginner" -> UiText.StringResource(R.string.filter_level_beginner)
        "intermediate" -> UiText.StringResource(R.string.filter_level_intermediate)
        "expert" -> UiText.StringResource(R.string.filter_level_expert)
        else -> UiText.StringResource(R.string.filter_value_not_specified)
    }
}

fun String?.toMechanicUiText(): UiText {
    return when (this?.lowercase()) {
        "compound" -> UiText.StringResource(R.string.filter_mechanic_compound)
        "isolation" -> UiText.StringResource(R.string.filter_mechanic_isolation)
        "n/a", null -> UiText.StringResource(R.string.filter_mechanic_na)
        else -> UiText.StringResource(R.string.filter_mechanic_na)
    }
}

fun String.toForceUiText(): UiText {
    return when (this.lowercase()) {
        "pull" -> UiText.StringResource(R.string.filter_force_pull)
        "push" -> UiText.StringResource(R.string.filter_force_push)
        "static" -> UiText.StringResource(R.string.filter_force_static)
        else -> UiText.StringResource(R.string.filter_value_not_specified)
    }
}
