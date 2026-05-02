package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.TodoItem
import com.ihor.thesystem.domain.model.TodoStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface TodoRepository {
    fun getTodosForDate(date: LocalDate): Flow<List<TodoItem>>
    suspend fun getTodosForDateSnapshot(date: LocalDate): List<TodoItem>
    suspend fun getTodoStatsForMonth(month: YearMonth): Map<LocalDate, TodoStats>
    suspend fun addTodo(date: LocalDate, title: String)
    suspend fun setTodoCompleted(todoId: Int, isCompleted: Boolean)
    suspend fun removeTodo(todoId: Int)
}
