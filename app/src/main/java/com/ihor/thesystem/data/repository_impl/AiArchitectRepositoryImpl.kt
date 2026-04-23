package com.ihor.thesystem.data.repository_impl

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class AiArchitectRepositoryImpl @Inject constructor(
    @Named("ArchitectModel") private val generativeModel: GenerativeModel,
    private val matrixRepo: ProgressionMatrixRepository
) : AiArchitectRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getChatResponse(prompt: String): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "null") {
            return@withContext ChatMessage(
                role = ChatRole.AI,
                text = "КРИТИЧНА ПОМИЛКА: API ключ не знайдено в BuildConfig. Зробіть Clean/Rebuild.",
                isActionable = false
            )
        }

        try {
            withTimeout(30_000L) {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: throw IllegalStateException("Empty AI response")

                val cleanJson = extractJson(responseText)
                val dto = try {
                    json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                } catch (e: Exception) {
                    Log.e("AiArchitect", "JSON Error: ${e.message}")
                    return@withTimeout ChatMessage(
                        role = ChatRole.AI,
                        text = responseText,
                        isActionable = false
                    )
                }

                val targets = dto.nextWorkoutTargets.map { target ->
                    AiWorkoutRecommendation(
                        exerciseId = target.exerciseId,
                        weight = target.weight,
                        sets = target.recommendedSets,
                        reps = target.recommendedReps,
                        aiFeedback = target.aiFeedback
                    )
                }

                // Збереження в БД через matrixRepo
                targets.forEach { rec ->
                    matrixRepo.updateTarget(
                        exerciseId = rec.exerciseId,
                        weight = rec.weight.toDouble(),
                        sets = rec.sets,
                        reps = rec.reps,
                        aiFeedback = rec.aiFeedback,
                        timestamp = System.currentTimeMillis()
                    )
                }

                ChatMessage(
                    role = ChatRole.AI,
                    text = dto.feedbackText.ifBlank { "Аналіз виконано." },
                    recommendations = targets,
                    isActionable = targets.isNotEmpty(),
                    aiFeedback = dto.aiFeedback ?: targets.firstOrNull()?.aiFeedback
                )
            }
        } catch (e: Exception) {
            Log.e("AiArchitect", "Request failed: ${e.message}")
            if (e is kotlinx.coroutines.CancellationException) throw e
            
            val errorMsg = when {
                e is kotlinx.serialization.SerializationException || 
                e.message?.contains("GrpcError") == true ||
                e.message?.contains("503") == true -> 
                    "Сервери AI тимчасово перевантажені. Спробуйте пізніше."
                    
                e.message?.contains("429") == true -> 
                    "Перевищено ліміт запитів (429). Спробуйте через хвилину."

                else -> "Помилка: ${e.localizedMessage}"
            }
            ChatMessage(
                role = ChatRole.AI,
                text = errorMsg,
                isActionable = false
            )
        }
    }

    private fun extractJson(input: String): String {
        val start = input.indexOf('{')
        val end = input.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return input.substring(start, end + 1)
        }
        return input.replace("```json", "").replace("```", "").trim()
    }
}
