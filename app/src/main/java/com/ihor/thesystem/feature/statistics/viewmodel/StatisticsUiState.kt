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
    val progressPercent: Float,
    val isActive: Boolean = true,
    val orderIndex: Int = 999
) {
    val displayTarget: String
        get() = if (targetWeight < 0f) targetWeightNote ?: "—"
        else "${targetWeight}кг"
    val displayCurrent: String
        get() = "${currentWeight}кг"
    val displayStart: String
        get() = "${startWeight}кг"
}

/**
 * Модель для вводу одного підходу (сету) в діалозі
 */
data class WorkoutSetInput(
    val id: Long = System.nanoTime(),
    val weight: String = "",
    val reps: String = ""
)

sealed class StatisticsDialogState {
    object None : StatisticsDialogState()
    
    // Етап 1: Встановлення цілей (Старт/Ціль)
    data class SetupMatrix(
        val entry: MatrixEntryUiModel,
        val startWeight: String,
        val targetWeight: String
    ) : StatisticsDialogState()
    
    // Етап 2: Логування підходів
    data class LogWorkoutSets(
        val entry: MatrixEntryUiModel,
        val sets: List<WorkoutSetInput> = listOf(
            WorkoutSetInput(),
            WorkoutSetInput(),
            WorkoutSetInput()
        )
    ) : StatisticsDialogState()
}
