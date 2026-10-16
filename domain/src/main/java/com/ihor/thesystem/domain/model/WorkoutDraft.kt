package com.ihor.thesystem.domain.model

data class WorkoutDraft(
    val id: String,
    val dateEpochDay: Long,
    val scheduleId: Int,
    val cycleDay: Int,
    val workoutTemplateId: Int?,
    val workoutName: String?,
    val questId: Long,
    val exercises: List<WorkoutDraftExercise>,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    init {
        require(id.isNotBlank()) { "Workout draft ID must not be blank" }
        require(exercises.isNotEmpty()) { "Workout draft must contain exercises" }
    }
}

data class WorkoutDraftExercise(
    val exerciseId: Int,
    val sets: List<WorkoutDraftSet>
)

data class WorkoutDraftSet(
    val inputId: Long,
    val weightInput: String,
    val repsInput: String,
    val isCompleted: Boolean
)

data class WorkoutDraftPlan(
    val dateEpochDay: Long,
    val scheduleId: Int,
    val cycleDay: Int,
    val workoutTemplateId: Int?,
    val exerciseIds: List<Int>
)

enum class WorkoutDraftCompatibility {
    COMPATIBLE,
    PREVIOUS_DAY,
    PLAN_CHANGED
}

fun WorkoutDraft.compatibilityWith(plan: WorkoutDraftPlan): WorkoutDraftCompatibility =
    when {
        dateEpochDay != plan.dateEpochDay -> WorkoutDraftCompatibility.PREVIOUS_DAY
        scheduleId != plan.scheduleId ||
            cycleDay != plan.cycleDay ||
            workoutTemplateId != plan.workoutTemplateId ||
            exercises.map { it.exerciseId } != plan.exerciseIds -> WorkoutDraftCompatibility.PLAN_CHANGED
        else -> WorkoutDraftCompatibility.COMPATIBLE
    }
