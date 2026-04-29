package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.QuestLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestLogDao {
    @Query("SELECT * FROM quest_log ORDER BY completedAt DESC")
    fun getFullHistory(): Flow<List<QuestLogEntity>>

    @Insert
    suspend fun insert(log: QuestLogEntity)

    @Query("""
        SELECT COUNT(*) FROM quest_log
        WHERE questType = :type
          AND wasSuccessful = 1
          AND completedAt BETWEEN :startMillis AND :endMillis
    """)
    fun getSuccessfulQuestCount(
        type: com.ihor.thesystem.data.local.room.entity.QuestType,
        startMillis: Long,
        endMillis: Long
    ): Flow<Int>

    @Query("SELECT * FROM quest_log ORDER BY completedAt DESC LIMIT :count")
    suspend fun getLastNLogs(count: Int): List<QuestLogEntity>
}
