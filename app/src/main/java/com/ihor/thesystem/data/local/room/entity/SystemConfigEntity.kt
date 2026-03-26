package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_config")
data class SystemConfigEntity(
    @PrimaryKey val id: Int = 1,
    val defaultPenalty: Int = 20,
    val targetSets: Int     = 3,
    val targetReps: Int     = 12,
    val matrixWeeks: Int    = 48
)