package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SystemConfigRepository {
    fun getConfigFlow(): Flow<SystemConfig?>
    suspend fun updateConfig(config: SystemConfig)
}

data class ActiveDebuff(
    val name: String,
    val penaltyPercent: Int
)

interface DebuffRepository {
    fun getActiveDebuffs(): Flow<List<ActiveDebuff>>
}

interface ScheduleRepository {
    fun getScheduleForDay(day: Int): Flow<ScheduleDay?>
    fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>>
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
