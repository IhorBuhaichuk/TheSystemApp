package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SystemConfigRepository {
    fun getConfigFlow(): Flow<SystemConfig?>
    suspend fun updateConfig(config: SystemConfig)
    suspend fun setNeedsDailyInit(needed: Boolean)
    suspend fun saveLastInitDate(epochDay: Long)
}

interface ScheduleRepository {
    fun getScheduleForDay(day: Int): Flow<ScheduleDay?>
    fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>>
    fun getAllExercises(): Flow<List<ExerciseDetails>>
    suspend fun createExercise(name: String): Int
    suspend fun deleteExercise(exerciseId: Int)
    suspend fun updateExerciseTrackingMode(exerciseId: Int, trackingMode: String?)
    suspend fun saveWorkoutForDay(cycleDay: Int, workoutName: String, exerciseIds: List<Int>)
    suspend fun removeExerciseFromDay(cycleDay: Int, exerciseId: Int)
}

sealed class DatabaseStatus {
    data object Idle : DatabaseStatus()
    data object Loading : DatabaseStatus()
    data object Ready : DatabaseStatus()
    data class Failed(val reason: String) : DatabaseStatus()
}

interface DatabaseReadinessRepository {
    val isDbReady: StateFlow<Boolean>
    val status: StateFlow<DatabaseStatus>
    fun markAsReady()
    fun markAsFailed(reason: String)
}
