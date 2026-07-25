package com.ihor.thesystem.data.remote.ai

import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiArchitectInsight
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

        val insight = AiArchitectInsight(
            weeklyInsight = dto.weeklyInsight.cleanBrief(MAX_INSIGHT_LENGTH),
            actionableSuggestions = dto.actionableSuggestions
                .map { it.cleanBrief(MAX_SUGGESTION_LENGTH) }
                .filter { it.isNotBlank() }
                .distinct()
                .take(MAX_SUGGESTIONS),
            recoveryRisk = dto.recoveryRisk.cleanBrief(MAX_RISK_LENGTH)
        ).takeIf { it.hasSignal }

        return ParsedAiArchitectResponse(
            feedbackText = dto.feedbackText.cleanBrief(MAX_FEEDBACK_LENGTH)
                .ifBlank { insight?.toFallbackFeedback().orEmpty() },
            recommendations = recommendations,
            aiFeedback = dto.aiFeedback?.cleanBrief(MAX_FEEDBACK_LENGTH)
                ?: recommendations.firstOrNull()?.aiFeedback?.cleanBrief(MAX_FEEDBACK_LENGTH),
            architectInsight = insight
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
    val aiFeedback: String?,
    val architectInsight: AiArchitectInsight?
)

private fun String.cleanBrief(maxLength: Int): String =
    trim()
        .replace(Regex("\\s+"), " ")
        .take(maxLength)
        .trim()

private fun AiArchitectInsight.toFallbackFeedback(): String =
    listOfNotNull(
        weeklyInsight.takeIf { it.isNotBlank() },
        recoveryRisk.takeIf { it.isNotBlank() },
        actionableSuggestions.firstOrNull { it.isNotBlank() }
    ).joinToString(" ")

private const val MAX_INSIGHT_LENGTH = 180
private const val MAX_RISK_LENGTH = 160
private const val MAX_FEEDBACK_LENGTH = 260
private const val MAX_SUGGESTION_LENGTH = 140
private const val MAX_SUGGESTIONS = 3
