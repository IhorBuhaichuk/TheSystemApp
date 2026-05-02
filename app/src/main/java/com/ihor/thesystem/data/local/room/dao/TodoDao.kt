package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ihor.thesystem.data.local.room.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query(
        """
        SELECT * FROM todo
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY isCompleted ASC, createdAtMillis ASC
        """
    )
    fun observeTodosForDate(dateEpochDay: Long): Flow<List<TodoEntity>>

    @Query(
        """
        SELECT * FROM todo
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY isCompleted ASC, createdAtMillis ASC
        """
    )
    suspend fun getTodosForDate(dateEpochDay: Long): List<TodoEntity>

    @Query(
        """
        SELECT * FROM todo
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay ASC, createdAtMillis ASC
        """
    )
    suspend fun getTodosForRange(startEpochDay: Long, endEpochDay: Long): List<TodoEntity>

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

    @Query("DELETE FROM todo WHERE id = :todoId")
    suspend fun delete(todoId: Int)
}
