package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayer(): Flow<Player?>
    fun getLatestWeight(): Flow<Float?>
    suspend fun updatePlayer(player: Player)
    suspend fun logWeight(weight: Float)
    suspend fun updateHeight(height: Float)
    suspend fun updateCurrentCycleDay(day: Int)
    suspend fun getWeightAtOrBefore(timestamp: Long): Float?
}
