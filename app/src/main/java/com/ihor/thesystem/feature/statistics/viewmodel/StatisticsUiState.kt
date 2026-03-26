package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.domain.repository.DailyTonnageStats

data class StatisticsUiData(
    val playerName: String                      = "",
    val playerClass: String                     = "",
    val currentMonth: Int                       = 1,
    val currentWeek: Int                        = 1,
    val currentCycleDay: Int                    = 1,
    val isPenaltyActive: Boolean                = false,
    val matrixEntries: List<MatrixEntryUiModel> = emptyList(),
    val tonnageStats: List<DailyTonnageStats>   = emptyList()
)

data class MatrixEntryUiModel(
    val exerciseId: Int,
    val exerciseName: String,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String?,
    val weeklyStep: Float,
    val progressPercent: Float
) {
    val displayTarget: String
        get() = if (targetWeight < 0f) targetWeightNote ?: "—"
        else "${targetWeight}кг"
    val displayCurrent: String
        get() = "${currentWeight}кг"
    val displayStart: String
        get() = "${startWeight}кг"
}

sealed class StatisticsDialogState {
    object None : StatisticsDialogState()
    data class EditWeight(val entry: MatrixEntryUiModel) : StatisticsDialogState()
}