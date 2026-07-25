package com.ihor.thesystem.feature.architect.viewmodel

import com.ihor.thesystem.data.remote.ai.AiAvailabilityState
import com.ihor.thesystem.domain.model.AiArchitectInsight
import com.ihor.thesystem.domain.model.ChatMessage

/**
 * Описує стан екрану AI Архітектора у форматі чату.
 */
data class ArchitectUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val lastWorkoutContext: String? = null,
    val analysisAlreadySent: Boolean = false,
    val aiAvailability: AiAvailabilityState = AiAvailabilityState.UNCONFIGURED,
    val latestInsight: AiArchitectInsightUiModel? = null
)

data class AiDashboardUiState(
    val isLoading: Boolean = true,
    val shortConclusion: String = "",
    val weeklyInsight: String = "",
    val actionableSuggestions: List<String> = emptyList(),
    val recoveryRisk: String = "",
    val lastRecommendation: AiRecommendationUiModel? = null
)

data class AiRecommendationUiModel(
    val exerciseName: String,
    val recommendedWeight: Double?,
    val recommendedSets: Int?,
    val recommendedReps: String?,
    val feedback: String?
)

data class AiArchitectInsightUiModel(
    val weeklyInsight: String,
    val actionableSuggestions: List<String>,
    val recoveryRisk: String
) {
    val hasSignal: Boolean
        get() = weeklyInsight.isNotBlank() ||
            actionableSuggestions.any { it.isNotBlank() } ||
            recoveryRisk.isNotBlank()
}

fun AiArchitectInsight.toUiModel(): AiArchitectInsightUiModel =
    AiArchitectInsightUiModel(
        weeklyInsight = weeklyInsight,
        actionableSuggestions = actionableSuggestions,
        recoveryRisk = recoveryRisk
    )
