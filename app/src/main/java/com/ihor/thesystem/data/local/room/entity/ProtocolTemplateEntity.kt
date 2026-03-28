package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocol_template")
data class ProtocolTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cycleDay: Int, // 1..4
    val taskName: String,
    val category: TaskCategory,
    val contextRequirement: ContextRequirement,
    val note: String? = null
)
