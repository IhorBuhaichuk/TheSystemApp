package com.ihor.thesystem.data.repository_impl

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class AiArchitectRepositoryImpl @Inject constructor(
    @Named("ArchitectModel") private val generativeModel: GenerativeModel
) : AiArchitectRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun getChatResponse(prompt: String): ChatMessage {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "null") {
            return ChatMessage(
                role = ChatRole.AI,
                text = "КРИТИЧНА ПОМИЛКА: Ключ Gemini API не знайдено. Перевірте local.properties.",
                isActionable = false
            )
        }

        var responseText = ""
        return try {
            withTimeout(60_000L) {
                val response = generativeModel.generateContent(prompt)
                responseText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")
                
                val cleanJson = extractJson(responseText)

                val dto = json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                val targets = dto.nextWorkoutTargets
                
                ChatMessage(
                    role = ChatRole.AI,
                    text = dto.feedbackText.ifBlank { "Аналіз завершено." },
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
            Log.e("AiArchitect", "Помилка парсингу або запиту Gemini: ${e.message}")
            if (responseText.isNotBlank()) {
                Log.e("AiArchitect", "Оригінальна відповідь AI (Raw Response): $responseText")
            }
            val errorDetail = e.localizedMessage ?: e.message ?: "Unknown error"
            ChatMessage(
                role = ChatRole.AI,
                text = "Виникла помилка під час аналізу. Спробуйте пізніше. [Error: $errorDetail]",
                isActionable = false
            )
        }
    }

    private fun extractJson(input: String): String {
        // Регулярний вираз для пошуку JSON блоку всередині Markdown
        val regex = Regex("""```json\s*([\s\S]*?)\s*```|```\s*([\s\S]*?)\s*```""")
        val matchResult = regex.find(input)
        
        return if (matchResult != null) {
            // Беремо вміст першої або другої групи захоплення (залежно від того, яка спрацювала)
            matchResult.groups[1]?.value ?: matchResult.groups[2]?.value ?: input
        } else {
            input
        }.trim()
    }
}
