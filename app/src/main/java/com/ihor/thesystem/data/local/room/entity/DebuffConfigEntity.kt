package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debuff_config")
data class DebuffConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val condition: String,
    val text: String,
    val penaltyPercent: Int = 0,
    val isActive: Boolean   = false
)