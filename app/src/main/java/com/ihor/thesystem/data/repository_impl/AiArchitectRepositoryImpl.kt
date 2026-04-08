package com.ihor.thesystem.data.repository_impl

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AiArchitectRepositoryImpl @Inject constructor() : AiArchitectRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("Ти елітний персональний тренер-аналітик. Твій стиль: професійний, природний, підтримуючий, але об'єктивний та лаконічний. " +
                 "ЗАБОРОНЕНО використовувати роботизований жаргон ('ІНІЦІАЛІЗАЦІЯ', 'СТАТУС', 'СИСТЕМА'), капслок для слів та надмірний пафос. " +
                 "Спілкуйся як реальна людина-експерт. Твоє завдання: проаналізувати поточний результат вправи, історію останніх тренувань, " +
                 "динаміку власної ваги гравця та його суб'єктивний фітбек. Порівняй цей прогрес із цільовими нормативами 'Річної Матриці Прогресії'. " +
                 "Надай коротку текстову оцінку (aiFeedback) до 3-х речень та чітку рекомендацію на наступне тренування (nextWeight, nextSets, nextReps). " +
                 "Відповідай СТРОГО у форматі JSON: {\"feedback_text\": \"Текст аналізу для користувача\", \"next_workout_targets\": [{\"exercise_id\": ID, \"nextWeight\": 0.0, \"nextSets\": 0, \"nextReps\": \"...\"}], \"aiFeedback\": \"Коротка оцінка для матриці (до 3 речень)\"}")
        }
    )

    override suspend fun getChatResponse(prompt: String): ChatMessage {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "null") {
            return ChatMessage(
                role = ChatRole.AI,
                text = "КРИТИЧНА ПОМИЛКА: Ключ Gemini API не знайдено. Перевірте local.properties.",
                isActionable = false
            )
        }

        return try {
            withTimeout(40_000L) {
                val response = generativeModel.generateContent(prompt)
                val rawText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")
                
                // Надійне очищення JSON від маркдаун-блоків
                val cleanJson = rawText
                    .replace(Regex("(?s)```json\\s*(.*?)\\s*```"), "$1")
                    .replace(Regex("(?s)```\\s*(.*?)\\s*```"), "$1")
                    .trim()

                val dto = json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                
                ChatMessage(
                    role = ChatRole.AI,
                    text = dto.feedbackText,
                    recommendations = dto.nextWorkoutTargets.map { 
                        AiWorkoutRecommendation(
                            exerciseId = it.exerciseId, 
                            weight = it.weight,
                            sets = it.recommendedSets,
                            reps = it.recommendedReps
                        )
                    },
                    isActionable = dto.nextWorkoutTargets.isNotEmpty(),
                    aiFeedback = dto.aiFeedback
                )
            }
        } catch (e: Exception) {
            Log.e("AiArchitect", "Помилка Gemini: ${e.message}")
            val errorDetail = e.localizedMessage ?: e.message ?: "Unknown error"
            ChatMessage(
                role = ChatRole.AI,
                text = "Виникла помилка під час аналізу. Спробуйте пізніше. [Error: $errorDetail]",
                isActionable = false
            )
        }
    }
}
