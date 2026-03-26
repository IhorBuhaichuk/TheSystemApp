package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.DebuffConfigDao
import com.ihor.thesystem.data.local.room.entity.DebuffConfigEntity
import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.repository.DebuffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DebuffRepositoryImpl @Inject constructor(
    private val dao: DebuffConfigDao
) : DebuffRepository {

    override fun getAllDebuffs(): Flow<List<DebuffConfig>> =
        dao.getAllDebuffs().map { list -> list.map { it.toDomain() } }

    override fun getActiveDebuffs(): Flow<List<DebuffConfig>> =
        dao.getActiveDebuffs().map { list -> list.map { it.toDomain() } }

    override suspend fun updateDebuff(debuff: DebuffConfig) =
        dao.update(debuff.toEntity())
}

private fun DebuffConfigEntity.toDomain() =
    DebuffConfig(id, condition, text, penaltyPercent, isActive)

private fun DebuffConfig.toEntity() =
    DebuffConfigEntity(id, condition, text, penaltyPercent, isActive)