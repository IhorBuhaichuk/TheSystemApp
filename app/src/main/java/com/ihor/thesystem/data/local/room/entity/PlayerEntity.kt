package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: Int = 1,
    val name: String        = "ІГОР",
    val level: Int          = 1,
    val playerClass: String = "ПРОБУДЖЕНИЙ",
    val height: Float       = 0f,
    val currentMonth: Int   = 1,
    val currentWeek: Int    = 1,
    val currentCycleDay: Int = 1,
    val consecutiveFailedMainQuests: Int = 0,
    val isPenaltyActive: Boolean = false
)