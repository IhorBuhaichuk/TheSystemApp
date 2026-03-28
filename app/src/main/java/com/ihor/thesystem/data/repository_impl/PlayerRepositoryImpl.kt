package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.PlayerDao
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.entity.PlayerEntity
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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

    override suspend fun updatePlayer(player: Player) =
        playerDao.insertOrUpdate(player.toEntity())

    override suspend fun logWeight(weight: Float) =
        weightLogDao.insert(WeightLogEntity(weight = weight))

    override suspend fun updateHeight(height: Float) {
        val player = playerDao.getPlayer().firstOrNull() ?: return
        playerDao.insertOrUpdate(player.copy(height = height))
    }

    override suspend fun updateCurrentCycleDay(day: Int) {
        val player = playerDao.getPlayer().firstOrNull() ?: return
        playerDao.insertOrUpdate(player.copy(currentCycleDay = day))
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
    isPenaltyActive = isPenaltyActive
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
    isPenaltyActive = isPenaltyActive
)
