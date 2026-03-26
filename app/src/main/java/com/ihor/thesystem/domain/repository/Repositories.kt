package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayer(): Flow<Player?>
    fun getLatestWeight(): Flow<Float?>
    suspend fun updatePlayer(player: Player)
    suspend fun logWeight(weight: Float)
}

interface QuestRepository {
    fun getActiveDailyQuest(): Flow<Quest?>
    fun getActiveMainQuest(): Flow<Quest?>
    suspend fun hasActiveQuests(): Boolean
    suspend fun toggleTaskCompletion(taskId: Int, questId: Int, isCompleted: Boolean)
    suspend fun updateQuestStatus(questId: Int, status: DomainQuestStatus)
    suspend fun createDailyQuest(title: String, tasks: List<String>, scheduleId: Int?)
    suspend fun createMainQuest(title: String, exercises: List<String>, scheduleId: Int?)

    suspend fun getLastTwoMainQuestsStatus(): List<DomainQuestStatus>
}

interface SystemConfigRepository {
    fun getConfig(): Flow<SystemConfig?>
    suspend fun updateConfig(config: SystemConfig)
}

interface DebuffRepository {
    fun getAllDebuffs(): Flow<List<DebuffConfig>>
    fun getActiveDebuffs(): Flow<List<DebuffConfig>>
    suspend fun updateDebuff(debuff: DebuffConfig)
}

interface ScheduleRepository {
    fun getScheduleForDay(day: Int): Flow<ScheduleDay?>
}

interface ProgressionMatrixRepository {
    fun getAllEntries(): Flow<List<ProgressionMatrixEntry>>
    suspend fun updateCurrentWeight(exerciseId: Int, newWeight: Float)
}

data class ProgressionMatrixEntry(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String?,
    val weeklyStep: Float,          // розраховується в UseCase
    val progressPercent: Float      // 0f..1f
)