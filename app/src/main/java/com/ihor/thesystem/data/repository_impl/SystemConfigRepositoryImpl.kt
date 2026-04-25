package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.SystemConfigDao
import com.ihor.thesystem.data.local.room.entity.SystemConfigEntity
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SystemConfigRepositoryImpl @Inject constructor(
    private val dao: SystemConfigDao
) : SystemConfigRepository {

    override fun getConfigFlow(): Flow<SystemConfig?> =
        dao.getConfigFlow().map { it?.toDomain() }

    override suspend fun updateConfig(config: SystemConfig) =
        dao.insertOrUpdate(config.toEntity())

    override suspend fun setNeedsDailyInit(needed: Boolean) {
        val current = dao.getConfigFlow().firstOrNull() ?: SystemConfigEntity()
        dao.insertOrUpdate(current.copy(needsDailyInit = needed))
    }
}

private fun SystemConfigEntity.toDomain() =
    SystemConfig(
        id = id,
        defaultPenalty = defaultPenalty,
        targetSets = targetSets,
        targetReps = targetReps,
        matrixWeeks = matrixWeeks,
        cycleAnchorDateTimestamp = cycleAnchorDateTimestamp,
        cycleAnchorDay = cycleAnchorDay,
        cycleDaysPerMicrocycle = cycleDaysPerMicrocycle,
        microCyclesPerMonth = microCyclesPerMonth,
        dayStartOffsetHours = dayStartOffsetHours,
        needsDailyInit = needsDailyInit
    )

private fun SystemConfig.toEntity() =
    SystemConfigEntity(
        id = id,
        defaultPenalty = defaultPenalty,
        targetSets = targetSets,
        targetReps = targetReps,
        matrixWeeks = matrixWeeks,
        cycleAnchorDateTimestamp = cycleAnchorDateTimestamp,
        cycleAnchorDay = cycleAnchorDay,
        cycleDaysPerMicrocycle = cycleDaysPerMicrocycle,
        microCyclesPerMonth = microCyclesPerMonth,
        dayStartOffsetHours = dayStartOffsetHours,
        needsDailyInit = needsDailyInit
    )
