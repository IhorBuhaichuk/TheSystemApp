package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_task",
    indices = [
        Index("questId"),
        Index("exerciseId")
    ]
)
data class QuestTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questId: Int,
    val name: String,
    val nameUk: String? = null,
    val isCompleted: Boolean = false,
    val exerciseId: Int? = null,
    val targetWeight: Double? = null,
    val targetSets: Int? = null,
    val targetReps: Int? = null
)
