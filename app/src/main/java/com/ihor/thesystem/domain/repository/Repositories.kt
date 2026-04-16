package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SystemConfigRepository {
    fun getConfigFlow(): Flow<SystemConfig?>
    suspend fun updateConfig(config: SystemConfig)
}

interface DebuffRepository {
    fun getAllDebuffs(): Flow<List<DebuffConfig>>
    fun getActiveDebuffs(): Flow<List<DebuffConfig>>
    suspend fun updateDebuff(debuff: DebuffConfig)
}

interface ScheduleRepository {
    fun getScheduleForDay(day: Int): Flow<ScheduleDay?>
    fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>>
}

sealed class DatabaseStatus {
    object Idle : DatabaseStatus()
    object Loading : DatabaseStatus()
    object Ready : DatabaseStatus()
    data class Failed(val reason: String) : DatabaseStatus()
}

interface DatabaseReadinessRepository {
    val isDbReady: StateFlow<Boolean>
    val status: StateFlow<DatabaseStatus>
    fun markAsReady()
    fun markAsFailed(reason: String)
}
