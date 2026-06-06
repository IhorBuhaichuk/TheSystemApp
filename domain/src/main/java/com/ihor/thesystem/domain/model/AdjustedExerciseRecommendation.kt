package com.ihor.thesystem.domain.model

data class AdjustedExerciseRecommendation(
    val exerciseId: Int,
    val baseWeight: Double,
    val adjustedWeight: Double,
    val baseSets: Int,
    val adjustedSets: Int,
    val baseReps: Int,
    val adjustedReps: Int,
    val adjustmentReason: String
)
