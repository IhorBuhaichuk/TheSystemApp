package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_directives")
data class WorkoutDirectiveEntity(
    @PrimaryKey
    val exerciseId: String,
    val targetWeight: Double,
    val targetSets: Int,
    val targetReps: Int
)