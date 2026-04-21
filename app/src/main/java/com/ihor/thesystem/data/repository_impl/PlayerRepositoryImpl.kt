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

    override suspend fun updatePlayer(player: Player): Result<Unit, DataError.Local> = runDbCatching {
        playerDao.insertOrUpdate(player.toEntity())
    }

    override suspend fun logWeight(weight: Float): Result<Unit, DataError.Local> = runDbCatching {
        weightLogDao.insert(WeightLogEntity(weight = weight))
    }

    override suspend fun updateHeight(height: Float): Result<Unit, DataError.Local> = runDbCatching {
        playerDao.updateHeight(height)
    }

    override suspend fun updateCurrentCycleDay(day: Int): Result<Unit, DataError.Local> = runDbCatching {
        playerDao.updateCurrentCycleDay(day)
    }

    override suspend fun getWeightAtOrBefore(timestamp: Long): Result<Float?, DataError.Local> = runDbCatching {
        weightLogDao.getWeightAtOrBefore(timestamp)?.weight
    }

    private suspend inline fun <T> runDbCatching(crossinline block: suspend () -> T): Result<T, DataError.Local> {
        return try {
            Result.Success(block())
        } catch (e: NoSuchElementException) {
            Result.Error(DataError.Local.NOT_FOUND)
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
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    xpTotal = xpTotal,
    xpThisWeek = xpThisWeek,
    chestAttr = chestAttr,
    backAttr = backAttr,
    shouldersAttr = shouldersAttr,
    quadsAttr = quadsAttr,
    legsAttr = legsAttr,
    armsAttr = armsAttr
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
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    xpTotal = xpTotal,
    xpThisWeek = xpThisWeek,
    chestAttr = chestAttr,
    backAttr = backAttr,
    shouldersAttr = shouldersAttr,
    quadsAttr = quadsAttr,
    legsAttr = legsAttr,
    armsAttr = armsAttr
)
