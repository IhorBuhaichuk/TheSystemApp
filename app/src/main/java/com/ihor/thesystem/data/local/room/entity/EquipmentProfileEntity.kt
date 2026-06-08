package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment_profile")
data class EquipmentProfileEntity(
    @PrimaryKey val id: Int = 1,
    val trainsAtGym: Boolean = false,
    val availableEquipment: String = "BODY_ONLY",
    val dumbbellMaxKg: Float? = null,
    val barbellAvailable: Boolean = false,
    val benchAvailable: Boolean = false,
    val pullUpBarAvailable: Boolean = false,
    val dipBarsAvailable: Boolean = false,
    val bandsAvailable: Boolean = false,
    val machinesAvailable: Boolean = false
)
