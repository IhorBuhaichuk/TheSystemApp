package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_log",
    indices = [
        Index("completedAt"),
        Index(value = ["questType", "wasSuccessful", "completedAt"])
    ]
)
data class QuestLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questId: Int,
    val questType: QuestType,
    val completedAt: Long,
    val wasSuccessful: Boolean
)
