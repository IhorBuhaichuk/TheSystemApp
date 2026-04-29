package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.QuestWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Transaction
    @Query("SELECT * FROM quest WHERE status = :status ORDER BY date DESC")
    fun getActiveQuestsWithTasks(status: QuestStatus): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("SELECT * FROM quest WHERE type = :type AND status = :status ORDER BY date DESC LIMIT 1")
    fun getActiveQuestByType(
        type: QuestType, 
        status: QuestStatus
    ): Flow<QuestWithTasks?>

    @Query("SELECT COUNT(*) FROM quest WHERE status = :status")
    suspend fun getActiveQuestCount(status: QuestStatus): Int

    @Query("SELECT * FROM quest_task WHERE questId = :questId")
    suspend fun getTasksForQuestSync(questId: Int): List<QuestTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestTask(task: QuestTaskEntity): Long

    @Update
    suspend fun updateQuestTask(task: QuestTaskEntity)

    @Query("UPDATE quest_task SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun setTaskCompletion(taskId: Int, isCompleted: Boolean)

    @Query("UPDATE quest SET status = :status WHERE id = :questId")
    suspend fun updateQuestStatus(questId: Int, status: QuestStatus)

    @Query("SELECT * FROM quest WHERE type = :type ORDER BY date DESC LIMIT :limit")
    suspend fun getLastQuestsByType(
        type: QuestType,
        limit: Int
    ): List<QuestEntity>

    @Query("DELETE FROM quest WHERE id = :questId")
    suspend fun deleteQuestById(questId: Int)

    @Query("DELETE FROM quest_task WHERE questId = :questId")
    suspend fun deleteTasksByQuestId(questId: Int)

    @Transaction
    suspend fun deleteQuestWithTasks(questId: Int) {
        deleteTasksByQuestId(questId)
        deleteQuestById(questId)
    }

    @Query("DELETE FROM quest_task WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("UPDATE quest SET status = :targetStatus WHERE status = :sourceStatus")
    suspend fun updateStatusForQuests(
        sourceStatus: QuestStatus,
        targetStatus: QuestStatus
    )

    @Transaction
    @Query("""
        SELECT * FROM quest 
        WHERE date >= :startOfDay AND date <= :endOfDay
        ORDER BY date DESC
    """)
    fun getQuestsByDateRange(startOfDay: Long, endOfDay: Long): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("""
        SELECT * FROM quest 
        WHERE date >= :startOfDay AND date <= :endOfDay
        AND (type = :typeDaily OR type = :typeMain)
        ORDER BY date DESC
    """)
    fun getDailyQuestsForDateRange(
        startOfDay: Long,
        endOfDay: Long,
        typeDaily: QuestType,
        typeMain: QuestType
    ): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("SELECT * FROM quest WHERE type = :questType AND status != :completedStatus ORDER BY date DESC")
    fun getPendingPromotionQuests(
        questType: QuestType,
        completedStatus: QuestStatus
    ): Flow<List<QuestWithTasks>>

    @Query("SELECT * FROM quest WHERE type = :type AND status = :status AND scheduleId = :exerciseId ORDER BY date DESC LIMIT 1")
    suspend fun getActivePromotionQuestByExercise(
        exerciseId: Int,
        type: QuestType,
        status: QuestStatus
    ): QuestEntity?

    @Transaction
    @Query("SELECT * FROM quest WHERE id = :id")
    suspend fun getQuestWithTasksById(id: Int): QuestWithTasks?

    @Transaction
    @Query("""
        SELECT qt.name, qt.isCompleted 
        FROM quest_task qt
        INNER JOIN quest q ON qt.questId = q.id
        WHERE q.date >= :startOfDay 
          AND q.date <= :endOfDay
          AND q.type = :questType
        ORDER BY qt.id ASC
    """)
    suspend fun getDailyTasksForDateSync(
        startOfDay: Long,
        endOfDay: Long,
        questType: com.ihor.thesystem.data.local.room.entity.QuestType
    ): List<TaskCompletionProjection>
}

data class TaskCompletionProjection(
    val name: String,
    val isCompleted: Boolean
)
