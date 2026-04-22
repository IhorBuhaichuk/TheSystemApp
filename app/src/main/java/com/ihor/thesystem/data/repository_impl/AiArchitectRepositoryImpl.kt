package com.ihor.thesystem.data.repository_impl

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.AppErrorType 
import com.ihor.thesystem.domain.model.DataError 
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.data.remote.dto.WorkoutTargetDto
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
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

                val dto = try {
                    json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                } catch (e: Exception) {
                    Log.e("AiArchitect", "JSON Decoding failed: ${e.message}")
                    return@withTimeout ChatMessage(
                        role = ChatRole.AI,
                        uiText = AppErrorType.AiParsingError.asUiText(),
                        text = "ДЕБАГ: ${e.localizedMessage}",
                        isActionable = false
                    )
                }

                val targets = dto.nextWorkoutTargets.map { it.toDomain() }

                // Save recommendations to database
                val now = System.currentTimeMillis()
                targets.forEach { rec ->
                    matrixRepo.updateTarget(
                        exerciseId = rec.exerciseId,
                        weight = rec.weight.toDouble(),
                        sets = rec.sets,
                        reps = rec.reps,
                        aiFeedback = rec.aiFeedback,
                        timestamp = now
                    )
                    if (rec.exerciseId <= 0) {
                        Log.e("AiArchitect", "Invalid exerciseId received from AI: ${rec.exerciseId}")
                    }
                }

                ChatMessage(
                    role = ChatRole.AI,
                    text = dto.feedbackText.ifBlank { "Аналіз завершено." },
                    recommendations = targets,
                    isActionable = targets.isNotEmpty(),
                    aiFeedback = dto.aiFeedback ?: targets.firstOrNull()?.aiFeedback
                )
            }
        } catch (e: Exception) {
            Log.e("AiArchitect", "Помилка парсингу або запиту Gemini: ${e.message}")
            val error: DomainError = when {
                e.message?.contains("429") == true -> DataError.Network.TOO_MANY_REQUESTS
                e.message?.contains("Too Many Requests", ignoreCase = true) == true -> DataError.Network.TOO_MANY_REQUESTS
                else -> AppErrorType.AiParsingError
            }
            ChatMessage(
                role = ChatRole.AI,
                uiText = error.asUiText(),
                text = "ДЕБАГ: ${e.localizedMessage}",
                isActionable = false
            )
        }
    }

    private fun WorkoutTargetDto.toDomain(): AiWorkoutRecommendation {
        Log.d("AiArchitect", "Mapping exercise ${this.exerciseId}: $this")
        
        return AiWorkoutRecommendation(
            exerciseId = exerciseId,
            weight = weight.takeIf { it >= 0 } ?: 0f,
            sets = recommendedSets.toInt().takeIf { it > 0 } ?: 1,
            reps = recommendedReps.toInt().toString().takeIf { it != "0" } ?: "8",
            aiFeedback = aiFeedback
        )
    }

    private fun sanitizeReps(reps: String): String {
        // Якщо AI повернув щось типу "8-10", залишаємо як є,
        // але якщо там сміття, намагаємось витягнути цифри або даємо дефолт "8"
        if (reps.isBlank()) return "8"
        val digitRegex = Regex("""\d+""")
        return if (digitRegex.containsMatchIn(reps)) {
            reps.trim()
        } else {
            "8"
        }
    }

    private fun extractJson(input: String): String {
        var trimmed = input.trim()
        
        // Якщо це вже чистий JSON
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        // Регулярний вираз для пошуку JSON блоку всередині Markdown як fallback
        val regex = Regex("""```json\s*([\s\S]*?)\s*```|```\s*([\s\S]*?)\s*```""")
        val matchResult = regex.find(trimmed)

        trimmed = if (matchResult != null) {
            matchResult.groups[1]?.value ?: matchResult.groups[2]?.value ?: trimmed
        } else {
            trimmed
        }.trim()

        // Спроба знайти JSON за дужками, якщо AI додав зайвий текст
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            val start = trimmed.indexOf('{')
            val end = trimmed.lastIndexOf('}')
            if (start != -1 && end != -1 && end > start) {
                trimmed = trimmed.substring(start, end + 1)
            }
        }

        return trimmed
    }
}
