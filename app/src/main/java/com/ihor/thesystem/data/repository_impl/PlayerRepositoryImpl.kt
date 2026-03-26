package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.PlayerDao
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.entity.PlayerEntity
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
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

    override suspend fun updatePlayer(player: Player) =
        playerDao.insertOrUpdate(player.toEntity())

    override suspend fun logWeight(weight: Float) =
        weightLogDao.insert(WeightLogEntity(weight = weight))
}

// ── Mappers ───────────────────────────────────────────────────────────────────
private fun PlayerEntity.toDomain() = Player(id, name, level, playerClass, height,
    currentMonth, currentWeek, currentCycleDay,
    consecutiveFailedMainQuests, isPenaltyActive)

private fun Player.toEntity() = PlayerEntity(id, name, level, playerClass, height,
    currentMonth, currentWeek, currentCycleDay,
    consecutiveFailedMainQuests, isPenaltyActive)