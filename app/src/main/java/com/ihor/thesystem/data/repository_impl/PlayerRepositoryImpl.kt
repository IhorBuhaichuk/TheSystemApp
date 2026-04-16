package com.ihor.thesystem.data.repository_impl

import android.database.sqlite.SQLiteException
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.data.local.room.dao.PlayerDao
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.entity.PlayerEntity
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao,
    private val weightLogDao: WeightLogDao
) : PlayerRepository {

    override fun getPlayer(): Flow<Player?> =
        playerDao.getPlayer().map { it?.toDomain() }

    override fun getLatestWeight(): Flow<Float?> =
        weightLogDao.getLatestLog().map { it?.weight }

    override suspend fun updatePlayer(player: Player): Result<Unit, DataError.Local> {
        return try {
            playerDao.insertOrUpdate(player.toEntity())
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun logWeight(weight: Float): Result<Unit, DataError.Local> {
        return try {
            weightLogDao.insert(WeightLogEntity(weight = weight))
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun updateHeight(height: Float): Result<Unit, DataError.Local> {
        return try {
            val player = playerDao.getPlayerSync() 
                ?: return Result.Error(DataError.Local.NOT_FOUND)
            playerDao.insertOrUpdate(player.copy(height = height))
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun updateCurrentCycleDay(day: Int): Result<Unit, DataError.Local> {
        return try {
            val player = playerDao.getPlayerSync()
                ?: return Result.Error(DataError.Local.NOT_FOUND)
            playerDao.insertOrUpdate(player.copy(currentCycleDay = day))
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun getWeightAtOrBefore(timestamp: Long): Result<Float?, DataError.Local> {
        return try {
            val weight = weightLogDao.getWeightAtOrBefore(timestamp)?.weight
            Result.Success(weight)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}

// ── Mappers ───────────────────────────────────────────────────────────────────
private fun PlayerEntity.toDomain() = Player(
    id = id, 
    name = name, 
    level = level, 
    playerClass = playerClass, 
    height = height,
    currentMonth = currentMonth, 
    currentWeek = currentWeek, 
    currentCycleDay = currentCycleDay,
    consecutiveMainQuestFailures = consecutiveMainQuestFailures, 
    isPenaltyActive = isPenaltyActive,
    globalRank = globalRank,
    strAttribute = strAttribute,
    endAttribute = endAttribute,
    disAttribute = disAttribute,
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    xpTotal = xpTotal,
    xpThisWeek = xpThisWeek
)

private fun Player.toEntity() = PlayerEntity(
    id = id, 
    name = name, 
    level = level, 
    playerClass = playerClass, 
    height = height,
    currentMonth = currentMonth, 
    currentWeek = currentWeek, 
    currentCycleDay = currentCycleDay,
    consecutiveMainQuestFailures = consecutiveMainQuestFailures, 
    isPenaltyActive = isPenaltyActive,
    globalRank = globalRank,
    strAttribute = strAttribute,
    endAttribute = endAttribute,
    disAttribute = disAttribute,
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    xpTotal = xpTotal,
    xpThisWeek = xpThisWeek
)
