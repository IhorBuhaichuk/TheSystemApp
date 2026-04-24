package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import timber.log.Timber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    override suspend fun getChatResponse(prompt: String): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "null") {
            Timber.e("Critical Error: Gemini API Key not found. Check local.properties.")
            return@withContext ChatMessage(
                role = ChatRole.AI,
                text = UiText.StringResource(R.string.error_ai_generic),
                isActionable = false
            )
        }

        try {
            repeat(3) { attempt ->
                try {
                    return@withContext withTimeout(30_000L) {
                        val response = generativeModel.generateContent(prompt)
                        val responseText = response.text ?: throw IllegalStateException("Empty AI response")

                        val cleanJson = extractJson(responseText)
                        val dto = try {
                            json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                        } catch (e: Exception) {
                            Timber.e("JSON parsing error: %s", e.message)
                            return@withTimeout ChatMessage(
                                role = ChatRole.AI,
                                text = UiText.DynamicString(responseText),
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

                        ChatMessage(
                            role = ChatRole.AI,
                            text = if (dto.feedbackText.isBlank()) {
                                UiText.StringResource(R.string.ai_analysis_complete)
                            } else {
                                UiText.DynamicString(dto.feedbackText)
                            },
                            recommendations = targets,
                            isActionable = targets.isNotEmpty(),
                            aiFeedback = dto.aiFeedback ?: targets.firstOrNull()?.aiFeedback
                        )
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    
                    val isRetryable = e is kotlinx.coroutines.TimeoutCancellationException || 
                                     e.message?.contains("429") == true
                    
                    if (attempt < 2 && isRetryable) {
                        val delayMs = if (attempt == 0) 1000L else 2000L
                        kotlinx.coroutines.delay(delayMs)
                    } else {
                        throw e
                    }
                }
            }
            // Should not be reached due to throw e in the last attempt
            error("Unexpected end of retry loop: this line should never execute")
        } catch (e: Exception) {
            Timber.e(e, "Request failed after retries")
            if (e is kotlinx.coroutines.CancellationException) throw e
            
            val errorText = when {
                e is kotlinx.serialization.SerializationException || 
                e.message?.contains("GrpcError") == true ||
                e.message?.contains("503") == true -> 
                    UiText.StringResource(R.string.error_ai_overloaded)
                    
                e.message?.contains("429") == true -> 
                    UiText.StringResource(R.string.error_ai_rate_limit)

                else -> UiText.StringResource(R.string.error_ai_generic)
            }
            ChatMessage(
                role = ChatRole.AI,
                text = errorText,
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
