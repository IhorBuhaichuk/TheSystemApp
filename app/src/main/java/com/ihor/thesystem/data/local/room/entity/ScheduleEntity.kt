package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cycleDay: Int,                     // 1–4
    val workoutTemplateId: Int?  = null,
    val debuffConfigId: Int?     = null
)