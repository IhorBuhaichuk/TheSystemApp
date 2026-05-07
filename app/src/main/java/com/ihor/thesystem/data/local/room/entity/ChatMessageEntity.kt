package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_message_table",
    indices = [
        Index(value = ["sessionId", "timestamp"]),
        Index(value = ["sessionId", "role"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val role: String, // "user" or "model"
    val message: String,
    val timestamp: Long
)
