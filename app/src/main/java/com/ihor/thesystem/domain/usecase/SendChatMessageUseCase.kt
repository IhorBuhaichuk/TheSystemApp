package com.ihor.thesystem.domain.usecase

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.data.local.room.dao.ChatDao
import com.ihor.thesystem.data.local.room.entity.ChatMessageEntity
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatDao: ChatDao,
    private val liveCoachRepository: LiveCoachRepository
) {
    suspend operator fun invoke(sessionId: Long, userMessage: String) {
        // 1. Зберігаємо повідомлення гравця в БД
        val userEntity = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            message = userMessage,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertChatMessage(userEntity)

        // 2. Беремо історію з БД для цього sessionId
        val historyEntities = chatDao.getChatHistory(sessionId).first()

        // 3. Конвертуємо об'єкти у формат Content для Gemini
        val history = historyEntities.map { entity ->
            content(role = if (entity.role == "user") "user" else "model") {
                text(entity.message)
            }
        }

        // 4. Відправляємо в репозиторій
        val coachResponse = liveCoachRepository.sendMessage(history.dropLast(1), userMessage)

        // 5. Зберігаємо відповідь у БД
        val modelEntity = ChatMessageEntity(
            sessionId = sessionId,
            role = "model",
            message = coachResponse,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertChatMessage(modelEntity)
    }
}
