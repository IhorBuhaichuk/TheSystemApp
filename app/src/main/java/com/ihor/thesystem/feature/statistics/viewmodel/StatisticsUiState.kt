package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import com.ihor.thesystem.feature.status.viewmodel.WorkoutSetInput
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class StatisticsUiData(
    val playerName: String                      = "",
    val playerClass: String                     = "",
    val currentMonth: Int                       = 1,
    val totalMonths: Int                        = 12,
    val currentWeek: Int                        = 1,
    val currentCycleDay: Int                    = 1,
    val isPenaltyActive: Boolean                = false,
    val globalRank: Rank                        = Rank.E,
    val currentWeight: Float                    = 0f,
    val currentHeight: Float                    = 0f,
    val matrixEntries: ImmutableList<MatrixEntryUiModel> = persistentListOf(),
    val tonnageStats: ImmutableList<DailyTonnageStats>   = persistentListOf(),
    val weightHistory: ImmutableList<WeightLogEntity>    = persistentListOf(),
    val characterAttributes: Map<MuscleGroup, Float>    = emptyMap()
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
    val weightHistory: ImmutableList<WeightHistoryEntry> = persistentListOf(),
    val nextRecommendedWeight: Double? = null,
    val nextRecommendedSets: Int? = null,
    val nextRecommendedReps: String? = null,
    val lastAiFeedback: String? = null
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
    data object None : StatisticsDialogState()
    
    data class SetupMatrix(
        val entry: MatrixEntryUiModel,
        val startWeight: String,
        val targetWeight: String
    ) : StatisticsDialogState()

    data class LogWorkoutSets(
        val entry: MatrixEntryUiModel,
        val sets: List<WorkoutSetInput>,
        val existingLog: ExerciseSet? = null
    ) : StatisticsDialogState()

    data object LogWeight : StatisticsDialogState()
    data object EditHeight : StatisticsDialogState()
}
