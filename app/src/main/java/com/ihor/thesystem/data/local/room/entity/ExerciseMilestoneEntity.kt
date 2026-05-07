package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_milestones",
    indices = [
        Index("exerciseId"),
        Index("achievedAt")
    ]
)
data class ExerciseMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val exerciseId: Int,
    val milestoneWeight: Double,
    val achievedAt: Long,
    val note: String? = null
)
