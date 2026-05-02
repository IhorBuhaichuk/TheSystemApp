package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_cycle_config")
data class CalendarCycleConfigEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val startEpochDay: Long,
    val repeats: Boolean,
    val template: String
)

@Entity(
    tableName = "calendar_cycle_day",
    primaryKeys = ["cycleId", "dayIndex"],
    indices = [Index("cycleId")],
    foreignKeys = [
        ForeignKey(
            entity = CalendarCycleConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CalendarCycleDayEntity(
    val cycleId: Int = 1,
    val dayIndex: Int,
    val name: String,
    val type: String
)
