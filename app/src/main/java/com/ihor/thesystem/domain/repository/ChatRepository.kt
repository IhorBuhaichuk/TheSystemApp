package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatHistory(sessionId: Long): Flow<List<ChatMessage>>
    suspend fun saveChatMessage(sessionId: Long, role: ChatRole, text: String)
    suspend fun getRecentHistory(sessionId: Long, limit: Int = 6): List<ChatMessage>
}
