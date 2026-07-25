package com.ihor.thesystem.domain.model

import java.util.UUID

enum class ChatRole { SYSTEM, USER, AI }

enum class AiConversationRole { USER, MODEL }

data class AiConversationMessage(
    val role: AiConversationRole,
    val text: String
)

enum class MessageTextKey {
    ERROR_AI_UNCONFIGURED,
    ERROR_AI_GENERIC,
    ERROR_AI_PARSING,
    ERROR_AI_RATE_LIMIT,
    ERROR_AI_OVERLOADED,
    AI_ANALYSIS_COMPLETE,
    AI_FALLBACK_ACTIVATED,
    ARCHITECT_INITIAL_MESSAGE,
    ARCHITECT_NO_DATA,
    ARCHITECT_SEND_ANALYSIS,
    ERROR_NETWORK_ARCHITECT,
    ARCHITECT_DIRECTIVES_APPLIED
}

sealed interface MessageText {
    data class DynamicString(val value: String) : MessageText
    data class Resource(
        val key: MessageTextKey,
        val args: List<String> = emptyList()
    ) : MessageText
}

data class AiWorkoutRecommendation(
    val exerciseId: Int, 
    val weight: Float, 
    val sets: Int, 
    val reps: String,
    val aiFeedback: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: MessageText = MessageText.DynamicString(""),
    val recommendations: List<AiWorkoutRecommendation> = emptyList(),
    val isActionable: Boolean = false,
    val aiFeedback: String? = null,
    val architectInsight: AiArchitectInsight? = null
)

data class AiArchitectInsight(
    val weeklyInsight: String = "",
    val actionableSuggestions: List<String> = emptyList(),
    val recoveryRisk: String = ""
) {
    val hasSignal: Boolean
        get() = weeklyInsight.isNotBlank() ||
            actionableSuggestions.any { it.isNotBlank() } ||
            recoveryRisk.isNotBlank()
}

/**
 * Базові моделі тренувань, що використовуються для аналізу Архітектором та в аналітиці.
 */
data class WorkoutSession(
    val sessionId: Long = 0L,
    val questId: Long,
    val timestamp: Long,
    val totalTonnage: Double,
    val cycleDay: Int,
    val durationMinutes: Int = 0
)

data class ExerciseSet(
    val setId: Long = 0L,
    val sessionId: Long,
    val exerciseId: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean,
    val userFeedback: String? = null
)

data class WorkoutLog(
    val session: WorkoutSession,
    val sets: List<ExerciseSet>
)

data class WeightHistoryEntry(
    val weight: Double,
    val timestamp: Long
)

data class WeightHistoryWithId(
    val weight: Double,
    val timestamp: Long,
    val exerciseId: Int
)

data class WorkoutDirective(
    val exerciseId: Int,
    val targetWeight: Double,
    val targetSets: Int,
    val targetReps: String
)

enum class SystemWorkoutGrade {
    S,
    A,
    B,
    C,
    D
}

enum class WorkoutPerformanceStatus {
    EXCEEDED,
    COMPLETED_WITH_RESERVE,
    COMPLETED_HARD,
    PARTIAL,
    FAILED
}

enum class WorkoutProgressionDecision {
    INCREASE_ALLOWED,
    HOLD,
    REDUCE,
    DELOAD_RECOMMENDED
}

data class SystemWorkoutJudgment(
    val grade: SystemWorkoutGrade,
    val completionPercent: Int,
    val performanceStatus: WorkoutPerformanceStatus,
    val progressionDecision: WorkoutProgressionDecision,
    val reason: String,
    val nextAction: String
)

/**
 * Комплексний звіт від AI Архітектора після аналізу тренування.
 * (Зберігаємо для сумісності під час рефакторингу)
 */
data class AiArchitectReport(
    val architectFeedback: MessageText,
    val currentStageStatus: String,
    val completedExercises: List<Int>,
    val pendingExercises: List<Int>,
    val nextWorkoutDirectives: List<WorkoutDirective>,
    val recoveryWindowHours: Double,
    val isFallback: Boolean,
    val sessionId: Long = 0L,
    val judgment: SystemWorkoutJudgment? = null
)
