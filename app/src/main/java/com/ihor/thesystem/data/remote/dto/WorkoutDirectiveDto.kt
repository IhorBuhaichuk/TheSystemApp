package com.ihor.thesystem.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDirectiveDto(
    val exerciseId: String,
    val targetWeight: Double,
    val targetSets: Int,
    val targetReps: Int
)