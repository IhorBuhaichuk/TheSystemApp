package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity

@Entity(
    tableName   = "workout_exercise_cross_ref",
    primaryKeys = ["workoutTemplateId", "exerciseId"]
)
data class WorkoutExerciseCrossRef(
    val workoutTemplateId: Int,
    val exerciseId: Int,
    val orderIndex: Int = 0
)