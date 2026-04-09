package com.ihor.thesystem.data.repository_impl

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.data.remote.dto.WorkoutTargetDto
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
            text("Ти фітнес-аналітик. Відповідай СУВОРО масивом об'єктів JSON. Кожен об'єкт має містити параметри на наступне тренування та поле aiFeedback (текст до 3 речень). КРИТИЧНО: Стандарт JSON вимагає виключно подвійних лапок. Щоб не зламати парсер, всередині тексту aiFeedback КАТЕГОРИЧНО ЗАБОРОНЕНО використовувати будь-які лапки (ні подвійні, ні одинарні) та переноси рядків (\\n).")
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
            withTimeout(60_000L) {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")
                
                // Очищення JSON за запитом
                val cleanJson = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val targets = try {
                    json.decodeFromString<List<WorkoutTargetDto>>(cleanJson)
                } catch (e: Exception) {
                    // Fallback на випадок якщо AI загорнув це в об'єкт з полем next_workout_targets
                    try {
                        json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson).nextWorkoutTargets
                    } catch (e2: Exception) {
                        Log.e("AiArchitect", "Failed to parse JSON: $cleanJson")
                        throw e2
                    }
                }
                
                ChatMessage(
                    role = ChatRole.AI,
                    text = "Аналіз завершено. Директиви вправ оновлено.",
                    recommendations = targets.map { 
                        AiWorkoutRecommendation(
                            exerciseId = it.exerciseId, 
                            weight = it.weight,
                            sets = it.recommendedSets,
                            reps = it.recommendedReps,
                            aiFeedback = it.aiFeedback
                        )
                    },
                    isActionable = targets.isNotEmpty(),
                    aiFeedback = targets.firstOrNull()?.aiFeedback
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
