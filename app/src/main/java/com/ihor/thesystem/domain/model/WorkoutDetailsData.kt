package com.ihor.thesystem.domain.model

data class WorkoutDetailsData(
    val dayNumber: Int,
    val workoutName: String?,
    val exercises: List<ExerciseWorkoutData>
)

data class ExerciseWorkoutData(
    val exerciseId: Int,
    val name: String,
    val nameUk: String?,
    val recommendedWeight: Double,
    val recommendedReps: Int,
    val recommendedSets: Int,
    val gifUrl: String?,
    val externalId: String?
) {
    val recommendation: String
        get() = "${recommendedSets}x${recommendedReps} @ ${recommendedWeight}kg"
}

