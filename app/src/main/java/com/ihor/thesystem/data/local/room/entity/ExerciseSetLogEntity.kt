package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionLogEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index("exerciseId"),
        Index(value = ["exerciseId", "sessionId"])
    ]
)
data class ExerciseSetLogEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Long = 0L,
    val sessionId: Long,
    val exerciseId: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean = true,
    val userFeedback: String? = null
)
