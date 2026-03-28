package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.*
import kotlinx.coroutines.flow.Flow

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
}
