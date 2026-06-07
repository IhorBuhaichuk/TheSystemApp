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
    val nameUk: String? = null,
    val category: ExerciseCategory = ExerciseCategory.UNKNOWN,
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val force: String? = null,
    val gifUrl: String? = null,
    val externalId: String? = null,
    val trackingMode: String? = null,
    val isCoreSystemExercise: Boolean = false,
    val movementPattern: String? = null,
    val techniqueTips: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val substitutionExternalIds: List<String> = emptyList()
)

data class ExerciseRecommendation(
    val exerciseId: Int,
    val exerciseName: String,
    val exerciseNameUk: String? = null,
    val weight: Double,
    val sets: Int,
    val reps: Int
)

enum class SystemWorkoutTemplateType(
    val questTitle: String,
    val completionXp: Int
) {
    NO_EXCUSE(
        questTitle = "NO EXCUSE PROTOCOL",
        completionXp = 30
    ),
    ACTIVE_RECOVERY(
        questTitle = "RECOVERY PROTOCOL",
        completionXp = 40
    ),
    DELOAD(
        questTitle = "DELOAD SESSION",
        completionXp = 60
    )
}
