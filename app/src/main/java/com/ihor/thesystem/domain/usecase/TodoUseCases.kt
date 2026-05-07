package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.TodoItem
import com.ihor.thesystem.domain.model.TodoStats
import com.ihor.thesystem.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetTodayTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val clock: AppClock
) {
    operator fun invoke(): Flow<List<TodoItem>> =
        todoRepository.getTodosForDate(clock.today())
}

class GetTodosForDateUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<TodoItem>> =
        todoRepository.getTodosForDate(date)
}

class GetTodoStatsForMonthUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(month: YearMonth): Map<LocalDate, TodoStats> =
        todoRepository.getTodoStatsForMonth(month)
}

class AddTodayTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(title: String, parentTodoId: Int? = null) {
        todoRepository.addTodo(clock.today(), title, parentTodoId)
    }
}

class AddTodayMicrotaskUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(parentTodoId: Int, title: String) {
        todoRepository.addTodo(clock.today(), title, parentTodoId)
    }
}

class ToggleTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(todoId: Int, currentCompletedState: Boolean) {
        todoRepository.setTodoCompleted(todoId, !currentCompletedState)
    }
}

class RemoveTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(todoId: Int) {
        todoRepository.removeTodo(todoId)
    }
}

class ReorderTodayTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(orderedTodoIds: List<Int>, parentTodoId: Int? = null) {
        todoRepository.reorderTodos(clock.today(), orderedTodoIds, parentTodoId)
    }
}

private fun AppClock.today(): LocalDate =
    Instant.ofEpochMilli(now())
        .atZone(zoneId())
        .toLocalDate()
