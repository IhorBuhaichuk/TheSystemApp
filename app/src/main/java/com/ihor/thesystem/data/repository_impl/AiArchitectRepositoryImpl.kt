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
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("Ти — жорсткий AI-тренер Системи. Твій стиль: кіберпанк, суворий, мотивуючий. " +
                 "Ти аналізуєш тренування і даєш вказівки. " +
                 "Відповідай СТРОГО у форматі JSON без зайвого тексту.")
        }
    )

    override suspend fun getChatResponse(prompt: String): ChatMessage {
        return try {
            withTimeout(20_000L) {
                val response = generativeModel.generateContent(prompt)
                val rawText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")
                
                // Безпечний парсинг маркдауну
                val cleanJson = rawText
                    .removePrefix("```json")
                    .removeSuffix("```")
                    .replace("```json", "") // на випадок якщо prefix не спрацював точно
                    .replace("```", "")
                    .trim()

                val dto = json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                
                ChatMessage(
                    role = ChatRole.AI,
                    text = dto.feedbackText,
                    recommendations = dto.nextWorkoutTargets.map { 
                        AiWorkoutRecommendation(it.exerciseId, it.weight, it.reps)
                    },
                    isActionable = dto.nextWorkoutTargets.isNotEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e("AiArchitect", "Помилка Gemini: ${e.message}")
            ChatMessage(
                role = ChatRole.AI,
                text = "Системна помилка зв'язку з архітектором. Спробуй пізніше. [Error: ${e.localizedMessage}]",
                isActionable = false
            )
        }
    }
}
