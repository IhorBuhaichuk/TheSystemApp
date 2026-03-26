package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_task_template")
data class DailyTaskTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)