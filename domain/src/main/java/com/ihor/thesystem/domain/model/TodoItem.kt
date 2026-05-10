package com.ihor.thesystem.domain.model

import java.time.LocalDate

data class TodoItem(
    val id: Int,
    val title: String,
    val date: LocalDate,
    val isCompleted: Boolean,
    val createdAtMillis: Long,
    val completedAtMillis: Long? = null,
    val parentTodoId: Int? = null,
    val sortOrder: Long = 0L,
    val microtasks: List<TodoItem> = emptyList()
)

data class TodoStats(
    val completedCount: Int,
    val totalCount: Int
)
