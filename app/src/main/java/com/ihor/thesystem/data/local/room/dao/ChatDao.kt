package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ihor.thesystem.data.local.room.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChatMessage(msg: ChatMessageEntity)

    @Query("SELECT * FROM chat_message_table WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getChatHistory(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_message_table WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 6")
    fun getRecentChatHistory(sessionId: Long): Flow<List<ChatMessageEntity>>
}
