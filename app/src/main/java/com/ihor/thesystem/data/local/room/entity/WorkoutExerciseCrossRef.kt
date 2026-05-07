package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName   = "workout_exercise_cross_ref",
    primaryKeys = ["workoutTemplateId", "exerciseId"],
    indices = [
        Index("exerciseId"),
        Index(value = ["workoutTemplateId", "orderIndex"])
    ]
)
data class WorkoutExerciseCrossRef(
    val workoutTemplateId: Int,
    val exerciseId: Int,
    val orderIndex: Int = 0
)
