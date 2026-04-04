package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.QuestWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Transaction
    @Query("SELECT * FROM quest WHERE status = :status ORDER BY date DESC")
    fun getActiveQuestsWithTasks(status: QuestStatus = QuestStatus.ACTIVE): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("SELECT * FROM quest WHERE type = :type AND status = :status LIMIT 1")
    fun getActiveQuestByType(
        type: QuestType, 
        status: QuestStatus = QuestStatus.ACTIVE
    ): Flow<QuestWithTasks?>

    @Query("SELECT COUNT(*) FROM quest WHERE status = :status")
    suspend fun getActiveQuestCount(status: QuestStatus = QuestStatus.ACTIVE): Int

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
        limit: Int = 2
    ): List<QuestEntity>

    @Query("DELETE FROM quest_task WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("UPDATE quest SET status = :targetStatus WHERE status = :sourceStatus")
    suspend fun archiveActiveQuests(
        sourceStatus: QuestStatus = QuestStatus.ACTIVE,
        targetStatus: QuestStatus = QuestStatus.FAILED
    )

    @Transaction
    @Query("""
        SELECT * FROM quest 
        WHERE date(date / 1000, 'unixepoch', 'localtime') = date(:dateMillis / 1000, 'unixepoch', 'localtime')
    """)
    fun getQuestsByDate(dateMillis: Long): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("""
        SELECT * FROM quest 
        WHERE date(date / 1000, 'unixepoch', 'localtime') = date(:dateMillis / 1000, 'unixepoch', 'localtime')
        AND (type = :typeDaily OR type = :typeMain)
    """)
    fun getDailyQuestsForDate(
        dateMillis: Long,
        typeDaily: QuestType = QuestType.DAILY,
        typeMain: QuestType = QuestType.MAIN
    ): Flow<List<QuestWithTasks>>

    @Transaction
    @Query("SELECT * FROM quest WHERE type = :questType AND status != :completedStatus")
    fun getPendingPromotionQuests(
        questType: QuestType = QuestType.PROMOTION,
        completedStatus: QuestStatus = QuestStatus.COMPLETED
    ): Flow<List<QuestWithTasks>>

    @Query("SELECT * FROM quest WHERE type = :type AND status = :status AND scheduleId = :exerciseId LIMIT 1")
    suspend fun getActivePromotionQuestByExercise(
        exerciseId: Int,
        type: QuestType = QuestType.PROMOTION,
        status: QuestStatus = QuestStatus.ACTIVE
    ): QuestEntity?

    @Transaction
    @Query("SELECT * FROM quest WHERE id = :id")
    suspend fun getQuestWithTasksById(id: Int): QuestWithTasks?
}
