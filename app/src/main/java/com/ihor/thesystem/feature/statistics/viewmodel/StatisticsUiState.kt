package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class StatisticsUiData(
    val playerName: String                      = "",
    val playerClass: String                     = "",
    val currentMonth: Int                       = 1,
    val currentWeek: Int                        = 1,
    val currentCycleDay: Int                    = 1,
    val isPenaltyActive: Boolean                = false,
    val globalRank: Rank                        = Rank.E,
    val matrixEntries: ImmutableList<MatrixEntryUiModel> = persistentListOf(),
    val tonnageStats: ImmutableList<DailyTonnageStats>   = persistentListOf()
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
    val currentRank: Rank = Rank.E,
    val completedCycles: Int = 0,
    val isActive: Boolean = true,
    val orderIndex: Int = 999,
    val weightHistory: ImmutableList<ExerciseWeightHistory> = persistentListOf()
) {
    val displayTarget: String
        get() = if (targetWeight < 0f) targetWeightNote ?: "—"
        else "${targetWeight}кг"
    val displayCurrent: String
        get() = "${currentWeight}кг"
    val displayStart: String
        get() = "${startWeight}кг"
}

data class WorkoutSetInput(
    val id: Long = System.nanoTime(),
    val weight: String = "",
    val reps: String = ""
)

sealed class StatisticsDialogState {
    object None : StatisticsDialogState()
    
    data class SetupMatrix(
        val entry: MatrixEntryUiModel,
        val startWeight: String,
        val targetWeight: String
    ) : StatisticsDialogState()
    
    data class LogWorkoutSets(
        val entry: MatrixEntryUiModel,
        val sets: ImmutableList<WorkoutSetInput> = persistentListOf(
            WorkoutSetInput(),
            WorkoutSetInput(),
            WorkoutSetInput()
        )
    ) : StatisticsDialogState()
}
