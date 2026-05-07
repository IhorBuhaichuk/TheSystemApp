package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo",
    indices = [
        Index("dateEpochDay"),
        Index("parentTodoId")
    ]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateEpochDay: Long,
    val parentTodoId: Int? = null,
    val sortOrder: Long = 0L,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long,
    val completedAtMillis: Long? = null
)
