package com.ihor.thesystem.domain.usecase

import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.ChatRepository
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import javax.inject.Inject

class SendLiveCoachMessageUseCase @Inject constructor(
    private val liveCoachRepository: LiveCoachRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: Long, userMessage: String): ChatMessage {
        // 1. Зберігаємо повідомлення гравця через репозиторій
        chatRepository.saveChatMessage(sessionId, ChatRole.USER, userMessage)

        // 2. Беремо історію через репозиторій (останні 6 повідомлень)
        val historyMessages = chatRepository.getRecentHistory(sessionId, 6)

        // 3. Конвертуємо об'єкти у формат Content для Gemini
        // Виключаємо останнє додане повідомлення з історії, бо воно передається окремо як userMessage
        // Або включаємо всі повідомлення крім останнього в history
        val history = historyMessages.map { msg ->
            content(role = if (msg.role == ChatRole.USER) "user" else "model") {
                text(msg.text)
            }
        }

        // 4. Відправляємо в репозиторій ШІ
        val coachResponse = try {
            liveCoachRepository.sendMessage(history.dropLast(1), userMessage)
        } catch (e: Exception) {
            "Помилка зв'язку з тренером: ${e.message}"
        }

        // 5. Зберігаємо відповідь через репозиторій
        chatRepository.saveChatMessage(sessionId, ChatRole.AI, coachResponse)
        
        return ChatMessage(role = ChatRole.AI, text = coachResponse)
    }
}
