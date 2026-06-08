package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.EquipmentProfileDao
import com.ihor.thesystem.data.local.room.entity.EquipmentProfileEntity
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EquipmentProfileRepositoryImpl @Inject constructor(
    private val dao: EquipmentProfileDao
) : EquipmentProfileRepository {

    override fun getProfile(): Flow<EquipmentProfile> =
        dao.getProfile().map { entity -> entity?.toDomain() ?: EquipmentProfile() }

    override suspend fun getProfileSnapshot(): EquipmentProfile =
        dao.getProfileSnapshot()?.toDomain() ?: EquipmentProfile()

    override suspend fun saveProfile(profile: EquipmentProfile) {
        dao.upsertProfile(profile.toEntity())
    }

    private fun EquipmentProfileEntity.toDomain(): EquipmentProfile =
        EquipmentProfile(
            trainsAtGym = trainsAtGym,
            availableEquipment = availableEquipment.toEquipmentSet(),
            dumbbellMaxKg = dumbbellMaxKg,
            barbellAvailable = barbellAvailable,
            benchAvailable = benchAvailable,
            pullUpBarAvailable = pullUpBarAvailable,
            dipBarsAvailable = dipBarsAvailable,
            bandsAvailable = bandsAvailable,
            machinesAvailable = machinesAvailable
        )

    private fun EquipmentProfile.toEntity(): EquipmentProfileEntity =
        EquipmentProfileEntity(
            id = 1,
            trainsAtGym = trainsAtGym,
            availableEquipment = availableEquipment.joinToString(separator = ",") { it.name },
            dumbbellMaxKg = dumbbellMaxKg,
            barbellAvailable = barbellAvailable,
            benchAvailable = benchAvailable,
            pullUpBarAvailable = pullUpBarAvailable,
            dipBarsAvailable = dipBarsAvailable,
            bandsAvailable = bandsAvailable,
            machinesAvailable = machinesAvailable
        )

    private fun String.toEquipmentSet(): Set<EquipmentType> =
        split(",")
            .mapNotNull { raw -> raw.trim().takeIf { it.isNotBlank() } }
            .mapNotNull { raw -> runCatching { EquipmentType.valueOf(raw) }.getOrNull() }
            .toSet()
            .ifEmpty { setOf(EquipmentType.BODY_ONLY) }
}
