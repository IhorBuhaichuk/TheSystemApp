package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest")
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String       = "",
    val type: QuestType,
    val date: Long          = System.currentTimeMillis(),
    val status: QuestStatus = QuestStatus.ACTIVE,
    val scheduleId: Int?    = null
)