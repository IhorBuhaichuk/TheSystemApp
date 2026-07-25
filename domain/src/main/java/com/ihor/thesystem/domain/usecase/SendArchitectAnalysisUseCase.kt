package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ChatRepository
import javax.inject.Inject

class SendArchitectAnalysisUseCase @Inject constructor(
    private val aiArchitectRepository: AiArchitectRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(workoutContext: String): ChatMessage {
        val response = aiArchitectRepository.getChatResponse(
            buildArchitectV2Prompt(workoutContext)
        )
        if (response.isCompletedArchitectAnalysis()) {
            chatRepository.saveChatMessage(
                sessionId = ARCHITECT_ANALYSIS_SESSION_ID,
                role = ChatRole.AI,
                text = response.persistableText()
            )
        }
        return response
    }

    private fun buildArchitectV2Prompt(workoutContext: String): String =
        """
        You are AI Architect v2 for THE SYSTEM: LEVEL UP.
        Principle: AI explains trends and suggests. The deterministic System decides.

        Analyze only the data inside <workout_data>.
        <workout_data>
        $workoutContext
        </workout_data>

        Required output:
        - Short weekly insight: one sentence.
        - Recovery/readiness risk: one sentence, neutral and practical.
        - 1-3 actionable suggestions. No more than three.
        - Optional next workout targets only for exercises that explicitly have external load.

        Safety rules:
        - Do not mutate the plan. You only propose. ValidateDirectivesUseCase is the final gatekeeper.
        - Do not bypass readiness, recovery debt, deload, no-excuse, rest, or progression matrix limits.
        - If readiness/recovery risk is not standard, prefer hold, recovery, deload, technique, sleep, or logging suggestions.
        - If an exercise block says no external load, do not add it to next_workout_targets and do not propose kg.
        - Use exercise_id only from <workout_data>. Never invent ids and never use 0.
        - Avoid motivational essays, shame, hype, medical claims, diagnoses, and cyberpunk roleplay.
        - User-facing string values must be Ukrainian, short, natural, and without markdown.

        Return only a valid JSON object with this schema:
        {
          "weekly_insight": "1 short Ukrainian sentence about the trend",
          "actionable_suggestions": [
            "short Ukrainian action 1",
            "short Ukrainian action 2"
          ],
          "recovery_risk": "1 short Ukrainian sentence about readiness or recovery risk",
          "feedback_text": "2 short Ukrainian sentences max; summary only, no numbering",
          "next_workout_targets": [
            {
              "exercise_id": 1,
              "nextWeight": 50.0,
              "nextSets": 3,
              "nextReps": "8-10",
              "aiFeedback": "one short Ukrainian sentence for this exercise"
            }
          ]
        }

        If there are no safe target changes, return next_workout_targets as an empty list and put the useful action in actionable_suggestions.
        Text fields must not contain quote characters inside values and must not contain newline characters.
        """.trimIndent()

    private fun ChatMessage.persistableText(): String =
        when (val value = text) {
            is MessageText.DynamicString -> value.value
            is MessageText.Resource -> aiFeedback ?: ARCHITECT_ANALYSIS_FALLBACK_TEXT
        }

    private fun ChatMessage.isCompletedArchitectAnalysis(): Boolean =
        recommendations.isNotEmpty() || architectInsight?.hasSignal == true
}

private const val ARCHITECT_ANALYSIS_SESSION_ID = 0L
private const val ARCHITECT_ANALYSIS_FALLBACK_TEXT = "Аналіз завершено."
