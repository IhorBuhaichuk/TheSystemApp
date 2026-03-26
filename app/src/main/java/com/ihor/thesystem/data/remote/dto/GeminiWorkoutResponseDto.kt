package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiWorkoutResponseDto(
    val architectFeedback: String,
    val currentStageStatus: String,
    val completedExercises: List<String>,
    val pendingExercises: List<String>,
    val nextWorkoutDirective: List<WorkoutDirectiveDto>
)