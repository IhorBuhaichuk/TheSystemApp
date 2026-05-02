package com.ihor.thesystem.data.remote.ai

import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class AiArchitectResponseParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
) {
    fun parse(responseText: String): ParsedAiArchitectResponse {
        val dto = try {
            json.decodeFromString<GeminiWorkoutResponseDto>(extractJsonObject(responseText))
        } catch (error: SerializationException) {
            throw AiMalformedResponseException("AI response is not valid architect JSON.", error)
        }

        val recommendations = dto.nextWorkoutTargets.mapNotNull { target ->
            val reps = target.recommendedReps.trim()
            val hasValidWeight = !target.weight.isNaN() && !target.weight.isInfinite() && target.weight >= 0f

            if (target.exerciseId <= 0 || target.recommendedSets <= 0 || reps.isBlank() || !hasValidWeight) {
                null
            } else {
                AiWorkoutRecommendation(
                    exerciseId = target.exerciseId,
                    weight = target.weight,
                    sets = target.recommendedSets,
                    reps = reps,
                    aiFeedback = target.aiFeedback
                )
            }
        }

        return ParsedAiArchitectResponse(
            feedbackText = dto.feedbackText.trim(),
            recommendations = recommendations,
            aiFeedback = dto.aiFeedback ?: recommendations.firstOrNull()?.aiFeedback
        )
    }

    internal fun extractJsonObject(input: String): String {
        val start = input.indexOf('{')
        if (start == -1) {
            throw AiMalformedResponseException("AI response does not contain a JSON object.")
        }

        var depth = 0
        var inString = false
        var escaped = false

        for (index in start until input.length) {
            val char = input[index]

            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
                continue
            }

            when (char) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return input.substring(start, index + 1)
                    }
                }
            }
        }

        throw AiMalformedResponseException("AI response JSON object is incomplete.")
    }
}

internal data class ParsedAiArchitectResponse(
    val feedbackText: String,
    val recommendations: List<AiWorkoutRecommendation>,
    val aiFeedback: String?
)
