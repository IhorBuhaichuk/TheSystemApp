package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.TodoDao
import com.ihor.thesystem.data.local.room.entity.TodoEntity
import com.ihor.thesystem.domain.model.TodoItem
import com.ihor.thesystem.domain.model.TodoStats
import com.ihor.thesystem.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val clock: AppClock
) : TodoRepository {

    override fun getTodosForDate(date: LocalDate): Flow<List<TodoItem>> =
        todoDao.observeTodosForDate(date.toEpochDay()).map { todos ->
            todos.map { it.toDomain() }
        }

    override suspend fun getTodosForDateSnapshot(date: LocalDate): List<TodoItem> =
        todoDao.getTodosForDate(date.toEpochDay()).map { it.toDomain() }

    override suspend fun getTodoStatsForMonth(month: YearMonth): Map<LocalDate, TodoStats> {
        val todos = todoDao.getTodosForRange(
            startEpochDay = month.atDay(1).toEpochDay(),
            endEpochDay = month.atEndOfMonth().toEpochDay()
        )
        return todos.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
            .mapValues { (_, dayTodos) ->
                TodoStats(
                    completedCount = dayTodos.count { it.isCompleted },
                    totalCount = dayTodos.size
                )
            }
    }

    override suspend fun addTodo(date: LocalDate, title: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return

        todoDao.insert(
            TodoEntity(
                title = normalizedTitle,
                dateEpochDay = date.toEpochDay(),
                createdAtMillis = clock.now()
            )
        )
    }

    override suspend fun setTodoCompleted(todoId: Int, isCompleted: Boolean) {
        todoDao.setCompleted(
            todoId = todoId,
            isCompleted = isCompleted,
            completedAtMillis = if (isCompleted) clock.now() else null
        )
    }

    override suspend fun removeTodo(todoId: Int) {
        todoDao.delete(todoId)
    }

    private fun TodoEntity.toDomain(): TodoItem =
        TodoItem(
            id = id,
            title = title,
            date = LocalDate.ofEpochDay(dateEpochDay),
            isCompleted = isCompleted,
            createdAtMillis = createdAtMillis,
            completedAtMillis = completedAtMillis
        )
}
