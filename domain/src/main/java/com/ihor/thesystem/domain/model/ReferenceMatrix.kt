package com.ihor.thesystem.domain.model

enum class ExerciseWeightType {
    ABSOLUTE,
    BODY_WEIGHT,
    ADDED_WEIGHT
}

data class ReferenceMatrix(
    val exerciseId: Int,
    val exerciseName: String,
    val weightType: ExerciseWeightType,
    val progressionStep: Double,
    val milestones: Map<String, Double>,
    val repsMilestones: Map<String, Int>? = null
)

