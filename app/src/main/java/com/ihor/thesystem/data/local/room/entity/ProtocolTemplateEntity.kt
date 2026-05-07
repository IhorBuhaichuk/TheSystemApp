package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "protocol_template",
    indices = [Index("cycleDay")]
)
data class ProtocolTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cycleDay: Int, // 1..4
    val taskName: String,
    val category: TaskCategory,
    val contextRequirement: ContextRequirement,
    val note: String? = null
)
