package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

import com.ihor.thesystem.domain.model.PlayerRank

data class StatisticsUiData(
    val playerName: String                      = "",
    val playerClass: PlayerRank                  = PlayerRank.NOVICE,
    val level: Int                              = 1,
    val xpTotal: Int                            = 0,
    val xpMax: Int                              = 1000,
    val currentMonth: Int                       = 1,
    val totalMonths: Int                        = 12,
    val currentWeek: Int                        = 1,
    val currentCycleDay: Int                    = 1,
    val isPenaltyActive: Boolean                = false,
    val globalRank: Rank                        = Rank.E,
    val currentWeight: Float                    = 0f,
    val currentHeight: Float                    = 0f,
    val age: Int                                = 0,
    val matrixEntries: ImmutableList<MatrixEntryUiModel> = persistentListOf(),
    val tonnageStats: ImmutableList<DailyTonnageStats>   = persistentListOf(),
    val weightHistory: ImmutableList<BodyWeightLog>      = persistentListOf(),
    val characterAttributes: Map<MuscleGroup, Float>    = emptyMap(),
    val currentStreak: Int                      = 0,
    val maxStreak: Int                          = 0,
    val xpThisWeek: Int                         = 0,
    val weeklySummary: WeeklyTrainingSummaryUiModel = WeeklyTrainingSummaryUiModel(),
    val progressProofs: ImmutableList<ProgressProofUiModel> = persistentListOf(),
    val weeklySystemReport: WeeklySystemReportUiModel = WeeklySystemReportUiModel(),
    val nutritionFloorStatus: NutritionFloorStatus = NutritionFloorStatus(),
    val systemInsight: SystemInsightUiModel = SystemInsightUiModel(),
    val betaMetrics: BetaMetricsUiModel = BetaMetricsUiModel(),
    val avatarUri: String? = null
)

data class WeeklyTrainingSummaryUiModel(
    val days: ImmutableList<WeeklyTrainingDayUiModel> = persistentListOf(),
    val workoutCount: Int = 0,
    val totalTonnage: Double = 0.0
)

data class WeeklyTrainingDayUiModel(
    val label: String,
    val workoutCount: Int,
    val totalTonnage: Double
)

data class ProgressProofUiModel(
    val exerciseName: String,
    val previousLabel: String,
    val currentLabel: String,
    val deltaText: String,
    val percentageChange: Float,
    val proofType: ProgressProofType
)

data class WeeklySystemReportUiModel(
    val bestTrainingDay: String = "",
    val weakestPattern: String = "",
    val biggestProgress: String = "",
    val recoveryIssue: String = "",
    val nextWeekDecision: String = ""
)

data class SystemInsightUiModel(
    val improved: String = "",
    val weakPoint: String = "",
    val recommendation: String = ""
)

data class BetaMetricsUiModel(
    val onboardingCompleted: Boolean = false,
    val firstWorkoutLogged: Boolean = false,
    val plannedCompletedThisWeek: Int = 0,
    val plannedMissedThisWeek: Int = 0,
    val currentStreak: Int = 0,
    val daysAppOpenedOrRefreshed: Int = 0,
    val decisionDistribution: ImmutableList<BetaDecisionDistributionUiModel> = persistentListOf()
) {
    val hasSignal: Boolean
        get() = onboardingCompleted ||
            firstWorkoutLogged ||
            plannedCompletedThisWeek > 0 ||
            plannedMissedThisWeek > 0 ||
            currentStreak > 0 ||
            daysAppOpenedOrRefreshed > 0 ||
            decisionDistribution.any { it.count > 0 }
}

data class BetaDecisionDistributionUiModel(
    val label: String,
    val count: Int
)

sealed class StatisticsDialogState {
    data object None : StatisticsDialogState()
    
    data class SetupMatrix(
        val entry: MatrixEntryUiModel,
        val startWeight: String,
        val targetWeight: String
    ) : StatisticsDialogState()

    data class LogWorkoutSets(
        val entry: MatrixEntryUiModel,
        val sets: List<ActiveSetInput>,
        val existingLog: ExerciseSet? = null
    ) : StatisticsDialogState()

    data object LogWeight : StatisticsDialogState()
    data object EditHeight : StatisticsDialogState()
    data object EditAge : StatisticsDialogState()
}
