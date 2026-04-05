package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GeminiWorkoutResponseDto(
    @SerialName("feedback_text") val feedbackText: String,
    @SerialName("next_workout_targets") val nextWorkoutTargets: List<WorkoutTargetDto> = emptyList()
)

@Serializable
data class WorkoutTargetDto(
    @SerialName("exercise_id") val exerciseId: Long,
    @SerialName("weight") val weight: Float,
    @SerialName("reps") val reps: Int
)
