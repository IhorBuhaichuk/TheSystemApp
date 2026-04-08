package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GeminiWorkoutResponseDto(
    @SerialName("feedback_text") val feedbackText: String = "",
    @SerialName("next_workout_targets") val nextWorkoutTargets: List<WorkoutTargetDto> = emptyList(),
    @SerialName("aiFeedback") val aiFeedback: String? = null
)

@Serializable
data class WorkoutTargetDto(
    @SerialName("exercise_id") val exerciseId: Long,
    @SerialName("nextWeight") val weight: Float,
    @SerialName("nextSets") val recommendedSets: Int,
    @SerialName("nextReps") val recommendedReps: String,
    @SerialName("aiFeedback") val aiFeedback: String? = null
)
