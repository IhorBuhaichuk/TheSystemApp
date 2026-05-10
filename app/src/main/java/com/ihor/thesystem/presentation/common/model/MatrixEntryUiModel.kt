package com.ihor.thesystem.presentation.common.model

import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.WeightHistoryEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MatrixEntryUiModel(
    val exerciseId: Int,
    val exerciseName: String,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String?,
    val weeklyStep: Float,
    val progressPercent: Float,
    val currentRank: Rank = Rank.E,
    val completedCycles: Int = 0,
    val isActive: Boolean = true,
    val orderIndex: Int = 999,
    val weightHistory: ImmutableList<WeightHistoryEntry> = persistentListOf(),
    val nextRecommendedWeight: Double? = null,
    val nextRecommendedSets: Int? = null,
    val nextRecommendedReps: String? = null,
    val lastAiFeedback: String? = null
) {
    val displayTarget: String
        get() = if (targetWeight < 0f) targetWeightNote ?: "вЂ”" else "${targetWeight}РєРі"
    val displayCurrent: String
        get() = "${currentWeight}РєРі"
    val displayStart: String
        get() = "${startWeight}РєРі"
}
