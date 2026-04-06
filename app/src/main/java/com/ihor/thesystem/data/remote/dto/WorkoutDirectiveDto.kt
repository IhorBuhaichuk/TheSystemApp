package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WorkoutDirectiveDto(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("target_weight") val targetWeight: Double,
    @SerialName("target_sets") val targetSets: Int,
    @SerialName("target_reps") val targetReps: String
)
