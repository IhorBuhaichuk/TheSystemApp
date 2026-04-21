package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.Rank

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: Int = 1,
    val name: String        = "Ігор",
    val level: Int          = 1,
    val playerClass: String = "Новачок",
    val height: Float       = 0f,
    val currentMonth: Int   = 1,
    val currentWeek: Int    = 1,
    val currentCycleDay: Int = 1,
    val consecutiveMainQuestFailures: Int = 0,
    val isPenaltyActive: Boolean = false,
    val globalRank: Rank = Rank.E,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpTotal: Int = 0,
    val xpThisWeek: Int = 0,
    val chestAttr: Int = 0,
    val backAttr: Int = 0,
    val shouldersAttr: Int = 0,
    val quadsAttr: Int = 0,
    val legsAttr: Int = 0,
    val armsAttr: Int = 0
)
