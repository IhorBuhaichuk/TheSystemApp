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

        var lastException: Exception? = null
        var currentDelay = 1000L

        repeat(3) { attempt ->
            try {
                return withTimeout(60_000L) {
                    val response = generativeModel.generateContent(prompt)
                    val responseText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")

                    val cleanJson = extractJson(responseText)

                    val dto = try {
                        json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                    } catch (e: Exception) {
                        Log.e("AiArchitect", "JSON Decoding failed on attempt ${attempt + 1}: ${e.message}")
                        // Якщо це помилка парсингу, повторні спроби навряд чи допоможуть, 
                        // але ми все одно спробуємо, раптом наступна відповідь буде кращою
                        throw e
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
                lastException = e
                Log.w("AiArchitect", "Attempt ${attempt + 1} failed: ${e.message}")
                
                // Перевіряємо чи варто робити повтор (Retry)
                val isRetryable = e is kotlinx.coroutines.TimeoutCancellationException ||
                        e.message?.contains("429") == true ||
                        e.message?.contains("Too Many Requests", ignoreCase = true) == true ||
                        e.message?.contains("SocketTimeout", ignoreCase = true) == true

                if (isRetryable && attempt < 2) {
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay *= 2
                } else {
                    // Якщо не підлягає повтору або це остання спроба - виходимо з try/catch
                    // і дозволяємо циклу repeat завершитись або викидаємо помилку обробки
                }
            }
        }

        // Обробка помилки після всіх спроб
        val error: DomainError = when {
            lastException?.message?.contains("429") == true -> DataError.Network.TOO_MANY_REQUESTS
            lastException?.message?.contains("Too Many Requests", ignoreCase = true) == true -> DataError.Network.TOO_MANY_REQUESTS
            lastException is kotlinx.coroutines.TimeoutCancellationException -> DataError.Network.REQUEST_TIMEOUT
            else -> AppErrorType.AiParsingError
        }

        return ChatMessage(
            role = ChatRole.AI,
            uiText = error.asUiText(),
            text = "ДЕБАГ (після 3 спроб): ${lastException?.localizedMessage}",
            isActionable = false
        )
    }

    private fun WorkoutTargetDto.toDomain(): AiWorkoutRecommendation {
        return AiWorkoutRecommendation(
            exerciseId = exerciseId,
            weight = weight,
            sets = recommendedSets,
            reps = recommendedReps,
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

        // Обов'язкове очищення від Markdown тегів перед парсингом
        trimmed = trimmed.replace(Regex("""^```json\s*|```$""", RegexOption.MULTILINE), "")
        trimmed = trimmed.trim()
        
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
