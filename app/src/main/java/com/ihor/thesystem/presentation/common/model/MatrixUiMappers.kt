package com.ihor.thesystem.presentation.common.model

import com.ihor.thesystem.domain.model.MatrixEntryData
import kotlinx.collections.immutable.toImmutableList

fun MatrixEntryData.toMatrixEntryUiModel(): MatrixEntryUiModel {
    val source = entry
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
        orderIndex = orderIndex,
        weightHistory = weightHistory.toImmutableList(),
        nextRecommendedWeight = source.nextRecommendedWeight,
        nextRecommendedSets = source.nextRecommendedSets,
        nextRecommendedReps = source.nextRecommendedReps,
        lastAiFeedback = source.lastAiFeedback
    )
}
