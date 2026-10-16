package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_drafts",
    indices = [
        Index("dateEpochDay"),
        Index("updatedAtMillis")
    ]
)
data class WorkoutDraftEntity(
    @PrimaryKey
    val draftId: String,
    val dateEpochDay: Long,
    val scheduleId: Int,
    val cycleDay: Int,
    val workoutTemplateId: Int?,
    val workoutName: String?,
    val questId: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "workout_draft_sets",
    primaryKeys = ["draftId", "exerciseOrder", "setOrder"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDraftEntity::class,
            parentColumns = ["draftId"],
            childColumns = ["draftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("draftId")]
)
data class WorkoutDraftSetEntity(
    val draftId: String,
    val exerciseId: Int,
    val exerciseOrder: Int,
    val setOrder: Int,
    val inputId: Long,
    val weightInput: String,
    val repsInput: String,
    val isCompleted: Boolean
)

