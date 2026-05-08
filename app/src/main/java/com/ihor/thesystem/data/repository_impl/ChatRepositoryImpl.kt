package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.ChatDao
import com.ihor.thesystem.data.local.room.entity.ChatMessageEntity
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val clock: AppClock
) : ChatRepository {

    override fun getChatHistory(sessionId: Long): Flow<List<ChatMessage>> {
        return chatDao.getChatHistory(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveChatMessage(sessionId: Long, role: ChatRole, text: String) {
        val entity = ChatMessageEntity(
            sessionId = sessionId,
            role = when (role) {
                ChatRole.USER -> "user"
                ChatRole.AI -> "model"
                ChatRole.SYSTEM -> "system"
            },
            message = text,
            timestamp = clock.now()
        )
        chatDao.insertChatMessage(entity)
    }

    override suspend fun getRecentHistory(sessionId: Long, limit: Int): List<ChatMessage> {
        val entities = chatDao.getRecentChatHistory(sessionId).first()
        return entities
            .sortedBy { it.timestamp }
            .takeLast(limit)
            .map { it.toDomain() }
    }

    override suspend fun hasAiResponse(sessionId: Long): Boolean {
        return chatDao.hasAiResponse(sessionId)
    }

    private fun ChatMessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id.toString(),
            role = when (role) {
                "user" -> ChatRole.USER
                "model" -> ChatRole.AI
                "system" -> ChatRole.SYSTEM
                else -> ChatRole.SYSTEM
            },
            text = MessageText.DynamicString(message)
        )
    }
}
