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

    // Модель gemini-2.5-flash
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("Ти — жорсткий AI-тренер Системи. Твій стиль: кіберпанк, суворий, мотивуючий. " +
                 "Ти аналізуєш тренування і даєш вказівки. " +
                 "Для кожної вправи обов'язково рекомендуй параметри на НАСТУПНЕ тренування. " +
                 "Базово - 3 підходи. При ефекті плато змінюй кількість підходів або повторень. " +
                 "Повертай поля: nextWeight (число), nextSets (число), nextReps (формат '12/10/8'). " +
                 "Відповідай СТРОГО у форматі JSON без зайвого тексту.")
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
            withTimeout(30_000L) {
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
                    isActionable = dto.nextWorkoutTargets.isNotEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e("AiArchitect", "Помилка Gemini: ${e.message}")
            val errorDetail = e.localizedMessage ?: e.message ?: "Unknown error"
            ChatMessage(
                role = ChatRole.AI,
                text = "Системна помилка зв'язку з архітектором. [Деталі: $errorDetail]. Спробуйте пізніше або перевірте налаштування моделі в AI Studio.",
                isActionable = false
            )
        }
    }
}
