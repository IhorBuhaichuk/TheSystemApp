package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ihor.thesystem.data.local.room.entity.EquipmentProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentProfileDao {

    @Query("SELECT * FROM equipment_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<EquipmentProfileEntity?>

    @Query("SELECT * FROM equipment_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSnapshot(): EquipmentProfileEntity?

    @Upsert
    suspend fun upsertProfile(profile: EquipmentProfileEntity)
}
