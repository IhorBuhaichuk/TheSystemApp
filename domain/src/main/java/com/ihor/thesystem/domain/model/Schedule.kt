package com.ihor.thesystem.domain.model

data class ScheduleDay(
    val id: Int,
    val cycleDay: Int,
    val workoutTemplateId: Int?,
    val workoutTemplateName: String?,
    val dailyTaskNames: List<String>,
    val exercises: List<ExerciseDetails>
) {
    val isWorkoutDay: Boolean get() = workoutTemplateId != null
}

data class ExerciseDetails(
    val id: Int,
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.UNKNOWN,
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val force: String? = null,
    val gifUrl: String? = null,
    val externalId: String? = null
)

data class ExerciseRecommendation(
    val exerciseId: Int,
    val exerciseName: String,
    val weight: Double,
    val sets: Int,
    val reps: Int
)
