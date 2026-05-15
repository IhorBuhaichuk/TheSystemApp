package com.ihor.thesystem.presentation.common.model

import com.ihor.thesystem.domain.model.MatrixEntryData
import com.ihor.thesystem.domain.model.WeightHistoryEntry
import com.ihor.thesystem.domain.repository.usesExternalLoad
import kotlinx.collections.immutable.toImmutableList

fun MatrixEntryData.toMatrixEntryUiModel(): MatrixEntryUiModel {
    val source = entry
    val canShowWeightMetrics = source.usesExternalLoad()
    return MatrixEntryUiModel(
        exerciseId = source.exerciseId,
        exerciseName = source.exerciseName,
        startWeight = source.startWeight,
        targetWeight = source.targetWeight,
        currentWeight = source.currentWeight,
        targetWeightNote = source.targetWeightNote,
        weeklyStep = source.weeklyStep,
        progressPercent = source.progressPercent,
        currentRank = source.currentRank,
        completedCycles = source.completedCycles,
        isActive = isActive,
        usesExternalLoad = canShowWeightMetrics,
        orderIndex = orderIndex,
        weightHistory = if (canShowWeightMetrics) {
            weightHistory.toImmutableList()
        } else {
            emptyList<WeightHistoryEntry>().toImmutableList()
        },
        nextRecommendedWeight = source.nextRecommendedWeight.takeIf { canShowWeightMetrics },
        nextRecommendedSets = source.nextRecommendedSets.takeIf { canShowWeightMetrics },
        nextRecommendedReps = source.nextRecommendedReps.takeIf { canShowWeightMetrics },
        lastAiFeedback = source.lastAiFeedback.takeIf { canShowWeightMetrics }
    )
}
