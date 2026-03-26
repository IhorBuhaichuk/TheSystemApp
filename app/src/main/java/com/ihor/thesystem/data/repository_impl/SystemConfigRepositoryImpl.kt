package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.SystemConfigDao
import com.ihor.thesystem.data.local.room.entity.SystemConfigEntity
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SystemConfigRepositoryImpl @Inject constructor(
    private val dao: SystemConfigDao
) : SystemConfigRepository {

    override fun getConfig(): Flow<SystemConfig?> =
        dao.getConfig().map { it?.toDomain() }

    override suspend fun updateConfig(config: SystemConfig) =
        dao.insertOrUpdate(config.toEntity())
}

private fun SystemConfigEntity.toDomain() =
    SystemConfig(id, defaultPenalty, targetSets, targetReps, matrixWeeks)

private fun SystemConfig.toEntity() =
    SystemConfigEntity(id, defaultPenalty, targetSets, targetReps, matrixWeeks)