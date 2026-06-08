package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.EquipmentProfile
import kotlinx.coroutines.flow.Flow

interface EquipmentProfileRepository {
    fun getProfile(): Flow<EquipmentProfile>
    suspend fun getProfileSnapshot(): EquipmentProfile
    suspend fun saveProfile(profile: EquipmentProfile)
}
