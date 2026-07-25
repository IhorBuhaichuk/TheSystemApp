package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AiDashboardData
import com.ihor.thesystem.domain.model.AiRecommendationSummary
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.usesExternalLoad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAiDashboardDataUseCase @Inject constructor(
    private val getStatisticsData: GetStatisticsDataUseCase
) {
    operator fun invoke(): Flow<AiDashboardData> {
        return getStatisticsData().map { statistics ->
            val shortConclusion = statistics.systemInsight.recommendation
                .ifBlank { statistics.systemInsight.weakPoint }
                .ifBlank { statistics.systemInsight.improved }
            val weeklyInsight = statistics.weeklySystemReport.nextWeekDecision
                .ifBlank { shortConclusion }
            val recoveryRisk = statistics.weeklySystemReport.recoveryIssue
                .ifBlank { "Система не бачить критичного ризику відновлення." }

            AiDashboardData(
                shortConclusion = shortConclusion,
                weeklyInsight = weeklyInsight,
                actionableSuggestions = listOf(
                    statistics.systemInsight.recommendation,
                    statistics.weeklySystemReport.nextWeekDecision,
                    statistics.weeklySystemReport.weakestPattern
                ).map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(MAX_DASHBOARD_SUGGESTIONS),
                recoveryRisk = recoveryRisk,
                lastRecommendation = statistics.matrixEntries
                    .map { it.entry }
                    .filter { it.hasAiRecommendation() }
                    .maxByOrNull { it.lastAnalyzedTimestamp }
                    ?.toAiRecommendationSummary()
            )
        }
    }

    private fun ProgressionMatrixEntry.hasAiRecommendation(): Boolean {
        return lastAnalyzedTimestamp > 0L &&
            usesExternalLoad() &&
            (nextRecommendedWeight != null ||
                nextRecommendedSets != null ||
                !nextRecommendedReps.isNullOrBlank() ||
                !lastAiFeedback.isNullOrBlank())
    }

    private fun ProgressionMatrixEntry.toAiRecommendationSummary(): AiRecommendationSummary {
        return AiRecommendationSummary(
            exerciseName = exerciseNameUk?.takeIf { it.isNotBlank() } ?: exerciseName,
            recommendedWeight = nextRecommendedWeight,
            recommendedSets = nextRecommendedSets,
            recommendedReps = nextRecommendedReps,
            feedback = lastAiFeedback,
            analyzedTimestamp = lastAnalyzedTimestamp
        )
    }

    private companion object {
        const val MAX_DASHBOARD_SUGGESTIONS = 3
    }
}
