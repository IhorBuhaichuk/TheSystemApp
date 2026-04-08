package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_message_table")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val role: String, // "user" or "model"
    val message: String,
    val timestamp: Long
)
