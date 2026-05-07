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
            todos.toTodoTree()
        }

    override suspend fun getTodosForDateSnapshot(date: LocalDate): List<TodoItem> =
        todoDao.getTodosForDate(date.toEpochDay()).toTodoTree()

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

    override suspend fun addTodo(date: LocalDate, title: String, parentTodoId: Int?) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return
        val dateEpochDay = date.toEpochDay()
        val nextSortOrder = (todoDao.getMaxSortOrder(dateEpochDay, parentTodoId) ?: 0L) + SORT_ORDER_STEP

        todoDao.insert(
            TodoEntity(
                title = normalizedTitle,
                dateEpochDay = dateEpochDay,
                parentTodoId = parentTodoId,
                sortOrder = nextSortOrder,
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

    override suspend fun reorderTodos(date: LocalDate, orderedTodoIds: List<Int>, parentTodoId: Int?) {
        if (orderedTodoIds.isEmpty()) return
        todoDao.updateSortOrders(
            dateEpochDay = date.toEpochDay(),
            parentTodoId = parentTodoId,
            orderedIds = orderedTodoIds.distinct()
        )
    }

    override suspend fun removeTodo(todoId: Int) {
        todoDao.deleteWithMicrotasks(todoId)
    }

    private fun List<TodoEntity>.toTodoTree(): List<TodoItem> {
        val childrenByParent = filter { it.parentTodoId != null }
            .groupBy { it.parentTodoId }

        return filter { it.parentTodoId == null }
            .map { parent ->
                parent.toDomain(
                    microtasks = childrenByParent[parent.id]
                        .orEmpty()
                        .map { it.toDomain() }
                )
            }
    }

    private fun TodoEntity.toDomain(): TodoItem =
        toDomain(microtasks = emptyList())

    private fun TodoEntity.toDomain(microtasks: List<TodoItem>): TodoItem =
        TodoItem(
            id = id,
            title = title,
            date = LocalDate.ofEpochDay(dateEpochDay),
            parentTodoId = parentTodoId,
            sortOrder = sortOrder,
            isCompleted = isCompleted,
            createdAtMillis = createdAtMillis,
            completedAtMillis = completedAtMillis,
            microtasks = microtasks
        )

    private companion object {
        const val SORT_ORDER_STEP = 1_000L
    }
}
