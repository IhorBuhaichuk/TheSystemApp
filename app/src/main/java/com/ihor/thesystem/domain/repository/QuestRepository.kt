package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    fun getActiveDailyQuest(): Flow<Quest?>
    fun getActiveMainQuest(): Flow<Quest?>
    fun getActivePromotionQuest(): Flow<Quest?>
    fun getActiveQuests(): Flow<List<Quest>>
    suspend fun hasActiveQuests(): Boolean
    suspend fun toggleTaskCompletion(taskId: Int, questId: Int, isCompleted: Boolean)
    suspend fun updateQuestStatus(questId: Int, status: DomainQuestStatus)
    suspend fun createDailyQuest(title: String, tasks: List<String>, scheduleId: Int?)
    suspend fun createMainQuest(title: String, exercises: List<ExerciseRecommendation>, scheduleId: Int?)
    suspend fun createPromotionQuest(
        exerciseId: Int, 
        title: String, 
        description: String,
        targetWeight: Double? = null,
        targetReps: Int? = null,
        exerciseNameUk: String? = null
    )
    suspend fun addTaskToQuest(questId: Int, taskName: String)
    suspend fun removeTask(taskId: Int)
    suspend fun archiveActiveQuests()

    suspend fun getLastTwoMainQuestsStatus(): List<DomainQuestStatus>
    fun getQuestsByDate(dateMillis: Long): Flow<List<Quest>>
    suspend fun getQuestById(questId: Int): Quest?
    suspend fun deleteQuestWithTasks(questId: Int)

    // Fixed methods
    fun getDailyQuestsForDate(dateMillis: Long): Flow<List<Quest>>
    fun getPendingPromotionQuests(): Flow<List<Quest>>
    fun getActivePromotionQuests(): Flow<List<Quest>>

    suspend fun logQuestResult(questId: Int, questType: DomainQuestType, wasSuccessful: Boolean)

    suspend fun getDailyTasksWithCompletionForDate(dateMillis: Long): List<Pair<String, Boolean>>
}
