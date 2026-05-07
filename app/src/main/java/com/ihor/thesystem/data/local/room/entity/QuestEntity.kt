package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest",
    indices = [
        Index("date"),
        Index(value = ["status", "date"]),
        Index(value = ["type", "status", "date"]),
        Index("targetExerciseId")
    ]
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String       = "",
    val type: QuestType,
    val date: Long,
    val status: QuestStatus = QuestStatus.ACTIVE,
    val scheduleId: Int?    = null,
    val targetExerciseId: Int? = null
)
