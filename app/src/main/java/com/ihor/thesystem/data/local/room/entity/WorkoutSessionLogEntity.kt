package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_session_logs",
    indices = [
        Index("questId"),
        Index("timestamp")
    ]
)
data class WorkoutSessionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0L,
    val questId: Long,
    val timestamp: Long,
    val totalTonnage: Double,
    val cycleDay: Int,
    val durationMinutes: Int = 0
)
