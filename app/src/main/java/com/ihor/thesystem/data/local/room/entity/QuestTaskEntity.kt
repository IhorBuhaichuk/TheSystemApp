package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_task")
data class QuestTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questId: Int,
    val name: String,
    val isCompleted: Boolean = false,
    val exerciseId: Int? = null,
    val recommendedWeight: Double? = null,
    val recommendedSets: Int? = null,
    val recommendedReps: Int? = null
)
