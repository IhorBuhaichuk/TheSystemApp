package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GeminiWorkoutResponseDto(
    @SerialName("status") val status: String = "success",
    @SerialName("feedback_text") val feedbackText: String = "",
    @SerialName("next_workout_targets") val nextWorkoutTargets: List<WorkoutTargetDto> = emptyList(),
    @SerialName("aiFeedback") val aiFeedback: String? = null
)

@Serializable
data class WorkoutTargetDto(
    @SerialName("exercise_id") val exerciseId: Int = 0,
    @SerialName("nextWeight") val weight: Float = 0f,
    @SerialName("nextSets") val recommendedSets: Float = 0f,
    @SerialName("nextReps") val recommendedReps: Float = 0f,
    @SerialName("aiFeedback") val aiFeedback: String? = null
)
