package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.BodyWeightLog
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayer(): Flow<Player?>
    fun getLatestWeight(): Flow<Float?>
    fun getWeightHistory(limit: Int = 100): Flow<List<BodyWeightLog>>
    suspend fun updatePlayer(player: Player): Result<Unit, DataError.Local>
    suspend fun logWeight(weight: Float): Result<Unit, DataError.Local>
    suspend fun updateHeight(height: Float): Result<Unit, DataError.Local>
    suspend fun updateAge(age: Int): Result<Unit, DataError.Local>
    suspend fun updateCurrentCycleDay(day: Int): Result<Unit, DataError.Local>
    suspend fun getWeightByDate(dateMillis: Long): Result<Float?, DataError.Local>
    suspend fun getWeightAtOrBefore(timestamp: Long): Result<Float?, DataError.Local>
}
