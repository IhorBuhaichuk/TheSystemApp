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

        // 2. Беремо історію (з запасом для фільтрації)
        val rawHistory = chatRepository.getRecentHistory(sessionId, 20)
        
        // 3. Фільтруємо та форматуємо історію для Gemini
        // Видаляємо повідомлення з помилками та системні, щоб не плутати ШІ
        val filteredHistory = rawHistory
            .dropLast(1) // Останнє - це щойно додане userMessage, воно йде окремим параметром
            .filter { it.role == ChatRole.USER || it.role == ChatRole.AI }
            .filter { !it.text.startsWith("Помилка зв'язку") }
            .takeLast(10) // Обмежуємо вікно пам'яті для економії токенів/квоти

        // Конвертуємо у формат Content та забезпечуємо чергування ролей (user/model)
        val validHistory = mutableListOf<com.google.ai.client.generativeai.type.Content>()
        var lastRole: String? = null

        for (msg in filteredHistory) {
            val role = if (msg.role == ChatRole.USER) "user" else "model"
            if (role != lastRole) {
                validHistory.add(content(role = role) { text(msg.text) })
                lastRole = role
            }
        }

        // КРИТИЧНО: Якщо історія закінчується на "user", Gemini не прийме наступний "userMessage".
        // Тому ми повинні видалити останній елемент історії, якщо це "user", щоб забезпечити чергування.
        if (lastRole == "user") {
            validHistory.removeAt(validHistory.size - 1)
        }

        // 4. Відправляємо запит до AI
        return try {
            val coachResponse = liveCoachRepository.sendMessage(validHistory, userMessage)
            
            // 5. Зберігаємо ТІЛЬКИ успішну відповідь
            chatRepository.saveChatMessage(sessionId, ChatRole.AI, coachResponse)
            
            ChatMessage(role = ChatRole.AI, text = coachResponse)
        } catch (e: Exception) {
            // КРИТИЧНО: Якщо корутину скасовано (наприклад, користувач вийшов з екрана),
            // ми ПОВИННІ прокинути CancellationException далі, щоб корутина зупинилася.
            if (e is kotlinx.coroutines.CancellationException) throw e
            
            // У разі інших помилок (наприклад, 429 або помилок серіалізації GrpcError)
            // повертаємо повідомлення, але НЕ зберігаємо його в БД.
            val errorText = when {
                e is kotlinx.serialization.SerializationException || 
                e.message?.contains("GrpcError") == true ||
                e.message?.contains("503") == true -> 
                    "Сервери AI тимчасово перевантажені. Спробуйте пізніше."
                
                e.message?.contains("429") == true -> 
                    "Помилка зв'язку з тренером: Перевищено ліміт запитів (Quota Exceeded). Спробуйте пізніше."

                else -> "Помилка зв'язку з тренером: ${e.localizedMessage ?: "невідома помилка"}"
            }
            ChatMessage(role = ChatRole.AI, text = errorText)
        }
    }
}
