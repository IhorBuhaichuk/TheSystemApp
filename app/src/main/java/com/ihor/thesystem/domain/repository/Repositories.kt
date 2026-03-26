package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayer(): Flow<Player?>
    fun getLatestWeight(): Flow<Float?>
    suspend fun updatePlayer(player: Player)
    suspend fun logWeight(weight: Float)
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
    suspend fun updateMatrixGoals(exerciseId: Int, startWeight: Float, targetWeight: Float)
    suspend fun saveExerciseSets(exerciseId: Int, sets: List<WorkoutSetInput>)
}

data class ProgressionMatrixEntry(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String?,
    val weeklyStep: Float,          
    val progressPercent: Float      
)
