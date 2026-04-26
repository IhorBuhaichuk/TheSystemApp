package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_config")
data class SystemConfigEntity(
    @PrimaryKey val id: Int = 1,
    val defaultPenalty: Int = 20,
    val targetSets: Int     = 3,
    val targetReps: Int     = 12,
    val matrixWeeks: Int    = 48,
    val cycleAnchorDateTimestamp: Long = 0L, // Epoch Day
    val cycleAnchorDay: Int = 1,             // Який це був день циклу (1..4)
    val cycleDaysPerMicrocycle: Int = 4,
    val microCyclesPerMonth: Int = 4,
    val needsDailyInit: Boolean = false
)
