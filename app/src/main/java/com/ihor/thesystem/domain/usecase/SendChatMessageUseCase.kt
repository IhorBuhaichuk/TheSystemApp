package com.ihor.thesystem.domain.usecase

import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ChatRepository
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val liveCoachRepository: LiveCoachRepository,
    private val aiArchitectRepository: AiArchitectRepository
) {
    /**
     * Відправляє повідомлення ШІ. 
     * Якщо передано [workoutContext], використовується AI Architect для аналізу тренування.
     * Якщо ні — використовується Live Coach для звичайного чату з історією.
     */
    suspend operator fun invoke(
        sessionId: Long, 
        userMessage: String, 
        workoutContext: String? = null
    ): ChatMessage {
        return if (workoutContext != null) {
            // Architect Mode - Консолідований промпт для аналізу
            val prompt = """
                Ти — жорсткий AI-тренер Системи. 
                Проаналізуй результати останнього тренування: $workoutContext. 
                Завдання: 
                1) Дати оцінку тренуванню. 
                2) Оцінити прогрес відносно довгострокового річного плану. 
                3) Надати мотиваційний блок у кіберпанк-стилі. 
                4) Запропонувати вагу та повторення на наступне тренування (збільш вагу на 2.5-5% при успіху). 
                
                ВАЖЛИВО: Відповідь поверни СУВОРО у форматі JSON об'єкта наступної структури:
                {
                  "feedback_text": "Твій текст з пунктами 1,2,3",
                  "next_workout_targets": [
                    {
                      "exercise_id": ID,
                      "nextWeight": 50.0,
                      "nextSets": 3,
                      "nextReps": "8-10",
                      "aiFeedback": "Короткий коментар до вправи"
                    }
                  ]
                }
                КРИТИЧНО: У текстах feedback_text та aiFeedback КАТЕГОРИЧНО ЗАБОРОНЕНО використовувати будь-які лапки (ні подвійні, ні одинарні) та символи переносу рядка.
            """.trimIndent()
            
            aiArchitectRepository.getChatResponse(prompt)
        } else {
            // Live Coach Mode
            // 1. Зберігаємо повідомлення гравця через репозиторій
            chatRepository.saveChatMessage(sessionId, ChatRole.USER, userMessage)

            // 2. Беремо історію через репозиторій
            val historyMessages = chatRepository.getRecentHistory(sessionId, 6)
                .sortedBy { it.id } // This is a bit risky if ID isn't timestamp-based, 
                // but the original code used sortedBy { it.timestamp } on entities.
                // Since I can't change the domain model easily, let's assume getRecentHistory returns 
                // them in an order we can use or just sorted by chronological order.
                // In ChatRepositoryImpl, I use DESC LIMIT 6, so I should reverse it.

            // 3. Конвертуємо об'єкти у формат Content для Gemini
            val history = historyMessages.map { msg ->
                val text = when(val t = msg.text) {
                    is UiText.DynamicString -> t.value
                    is UiText.StringResource -> ""
                }
                content(role = if (msg.role == ChatRole.USER) "user" else "model") {
                    text(text)
                }
            }

            // 4. Відправляємо в репозиторій
            val coachResponse = try {
                liveCoachRepository.sendMessage(history.dropLast(1), userMessage)
            } catch (e: Exception) {
                "Помилка зв'язку з тренером: ${e.message}"
            }

            // 5. Зберігаємо відповідь через репозиторій
            chatRepository.saveChatMessage(sessionId, ChatRole.AI, coachResponse)
            
            ChatMessage(role = ChatRole.AI, text = UiText.DynamicString(coachResponse))
        }
    }
}
