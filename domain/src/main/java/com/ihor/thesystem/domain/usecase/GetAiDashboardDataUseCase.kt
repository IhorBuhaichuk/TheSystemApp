package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AiDashboardData
import com.ihor.thesystem.domain.model.AiRecommendationSummary
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAiDashboardDataUseCase @Inject constructor(
    private val getStatisticsData: GetStatisticsDataUseCase
) {
    operator fun invoke(): Flow<AiDashboardData> {
        return getStatisticsData().map { statistics ->
            AiDashboardData(
                shortConclusion = statistics.systemInsight.recommendation
                    .ifBlank { statistics.systemInsight.weakPoint }
                    .ifBlank { statistics.systemInsight.improved },
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
}
