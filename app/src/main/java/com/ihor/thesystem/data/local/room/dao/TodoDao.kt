package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ihor.thesystem.data.local.room.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query(
        """
        SELECT * FROM todo
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY parentTodoId IS NOT NULL ASC, parentTodoId ASC, sortOrder ASC, createdAtMillis ASC
        """
    )
    fun observeTodosForDate(dateEpochDay: Long): Flow<List<TodoEntity>>

    @Query(
        """
        SELECT * FROM todo
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY parentTodoId IS NOT NULL ASC, parentTodoId ASC, sortOrder ASC, createdAtMillis ASC
        """
    )
    suspend fun getTodosForDate(dateEpochDay: Long): List<TodoEntity>

    @Query(
        """
        SELECT * FROM todo
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay ASC, parentTodoId IS NOT NULL ASC, parentTodoId ASC, sortOrder ASC, createdAtMillis ASC
        """
    )
    suspend fun getTodosForRange(startEpochDay: Long, endEpochDay: Long): List<TodoEntity>

    @Query(
        """
        SELECT MAX(sortOrder) FROM todo
        WHERE dateEpochDay = :dateEpochDay
          AND (
              (:parentTodoId IS NULL AND parentTodoId IS NULL)
              OR parentTodoId = :parentTodoId
          )
        """
    )
    suspend fun getMaxSortOrder(dateEpochDay: Long, parentTodoId: Int?): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(todo: TodoEntity)

    @Query(
        """
        UPDATE todo
        SET isCompleted = :isCompleted,
            completedAtMillis = :completedAtMillis
        WHERE id = :todoId
        """
    )
    suspend fun setCompleted(todoId: Int, isCompleted: Boolean, completedAtMillis: Long?)

    @Query(
        """
        UPDATE todo
        SET sortOrder = :sortOrder
        WHERE id = :todoId
          AND dateEpochDay = :dateEpochDay
          AND (
              (:parentTodoId IS NULL AND parentTodoId IS NULL)
              OR parentTodoId = :parentTodoId
          )
        """
    )
    suspend fun updateSortOrder(
        todoId: Int,
        dateEpochDay: Long,
        parentTodoId: Int?,
        sortOrder: Long
    )

    @Query("DELETE FROM todo WHERE id = :todoId OR parentTodoId = :todoId")
    suspend fun deleteWithMicrotasks(todoId: Int)

    @Transaction
    suspend fun updateSortOrders(
        dateEpochDay: Long,
        parentTodoId: Int?,
        orderedIds: List<Int>
    ) {
        orderedIds.forEachIndexed { index, todoId ->
            updateSortOrder(
                todoId = todoId,
                dateEpochDay = dateEpochDay,
                parentTodoId = parentTodoId,
                sortOrder = (index + 1L) * SORT_ORDER_STEP
            )
        }
    }

    private companion object {
        const val SORT_ORDER_STEP = 1_000L
    }
}
