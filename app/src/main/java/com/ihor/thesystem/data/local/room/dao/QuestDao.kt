package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.QuestWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Transaction
    @Query("SELECT * FROM quest WHERE status = 'ACTIVE' ORDER BY date DESC")
    fun getActiveQuestsWithTasks(): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("SELECT * FROM quest WHERE type = :type AND status = 'ACTIVE' LIMIT 1")
    fun getActiveQuestByType(type: QuestType): Flow<QuestWithTasks?>

    @Query("SELECT COUNT(*) FROM quest WHERE status = 'ACTIVE'")
    suspend fun getActiveQuestCount(): Int

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

    @Query("SELECT * FROM quest WHERE type = 'MAIN' ORDER BY date DESC LIMIT 2")
    suspend fun getLastTwoMainQuests(): List<QuestEntity>
}