package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    fun getActiveDailyQuest(): Flow<Quest?>
    fun getActiveMainQuest(): Flow<Quest?>
    suspend fun hasActiveQuests(): Boolean
    suspend fun toggleTaskCompletion(taskId: Int, questId: Int, isCompleted: Boolean)
    suspend fun updateQuestStatus(questId: Int, status: DomainQuestStatus)
    suspend fun createDailyQuest(title: String, tasks: List<String>, scheduleId: Int?)
    
    /**
     * Creates a main quest with structured exercise recommendations.
     */
    suspend fun createMainQuest(title: String, exercises: List<ExerciseRecommendation>, scheduleId: Int?)

    suspend fun addTaskToQuest(questId: Int, taskName: String)
    suspend fun removeTask(taskId: Int)

    suspend fun getLastTwoMainQuestsStatus(): List<DomainQuestStatus>
    fun getQuestsByDate(dateMillis: Long): Flow<List<Quest>>
}
