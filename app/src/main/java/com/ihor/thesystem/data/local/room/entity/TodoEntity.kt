package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo",
    indices = [Index("dateEpochDay")]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateEpochDay: Long,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long,
    val completedAtMillis: Long? = null
)
