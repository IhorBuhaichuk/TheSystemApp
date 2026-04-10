package com.ihor.thesystem.domain.usecase

import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.data.local.room.dao.ChatDao
import com.ihor.thesystem.data.local.room.entity.ChatMessageEntity
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatDao: ChatDao,
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
            // 1. Зберігаємо повідомлення гравця в БД
            val userEntity = ChatMessageEntity(
                sessionId = sessionId,
                role = "user",
                message = userMessage,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertChatMessage(userEntity)

            // 2. Беремо історію з БД для цього sessionId (обмежено 6 повідомленнями)
            val historyEntities = chatDao.getRecentChatHistory(sessionId).first()
                .sortedBy { it.timestamp }

            // 3. Конвертуємо об'єкти у формат Content для Gemini
            val history = historyEntities.map { entity ->
                content(role = if (entity.role == "user") "user" else "model") {
                    text(entity.message)
                }
            }

            // 4. Відправляємо в репозиторій
            val coachResponse = try {
                liveCoachRepository.sendMessage(history.dropLast(1), userMessage)
            } catch (e: Exception) {
                "Помилка зв'язку з тренером: ${e.message}"
            }

            // 5. Зберігаємо відповідь у БД
            val modelEntity = ChatMessageEntity(
                sessionId = sessionId,
                role = "model",
                message = coachResponse,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertChatMessage(modelEntity)
            
            ChatMessage(role = ChatRole.AI, text = coachResponse)
        }
    }
}
