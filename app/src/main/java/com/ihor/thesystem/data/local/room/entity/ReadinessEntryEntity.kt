package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.ReadinessLevel

@Entity(
    tableName = "readiness_entries",
    indices = [
        Index(value = ["dateEpochDay"], unique = true),
        Index("createdAtMillis")
    ]
)
data class ReadinessEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dateEpochDay: Long,
    val sleepHours: Float? = null,
    val energy: Int? = null,
    val stress: Int? = null,
    val soreness: Int? = null,
    val motivation: Int? = null,
    val note: String? = null,
    val score: Int,
    val level: ReadinessLevel,
    val createdAtMillis: Long
)
