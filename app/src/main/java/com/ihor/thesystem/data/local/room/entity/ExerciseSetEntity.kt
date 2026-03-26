package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Long = 0L,
    val sessionId: Long,
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean
)