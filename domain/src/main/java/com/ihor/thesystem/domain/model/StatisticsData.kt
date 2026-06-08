package com.ihor.thesystem.domain.model

import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import java.time.LocalDate

data class StatisticsData(
    val playerName: String = "",
    val playerClass: PlayerRank = PlayerRank.NOVICE,
    val level: Int = 1,
    val xpTotal: Int = 0,
    val xpMax: Int = 1000,
    val currentMonth: Int = 1,
    val totalMonths: Int = 12,
    val currentWeek: Int = 1,
    val currentCycleDay: Int = 1,
    val isPenaltyActive: Boolean = false,
    val globalRank: Rank = Rank.E,
    val currentWeight: Float = 0f,
    val currentHeight: Float = 0f,
    val age: Int = 0,
    val matrixEntries: List<MatrixEntryData> = emptyList(),
    val weightHistory: List<BodyWeightLog> = emptyList(),
    val characterAttributes: Map<MuscleGroup, Float> = emptyMap(),
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpThisWeek: Int = 0,
    val weeklySummary: WeeklyTrainingSummary = WeeklyTrainingSummary(),
    val progressProofs: List<ProgressProof> = emptyList(),
    val weeklySystemReport: WeeklySystemReport = WeeklySystemReport(),
    val nutritionFloorStatus: NutritionFloorStatus = NutritionFloorStatus(),
    val systemInsight: SystemInsight = SystemInsight(),
    val avatarUri: String? = null
)

data class MatrixEntryData(
    val entry: ProgressionMatrixEntry,
    val isActive: Boolean,
    val orderIndex: Int,
    val weightHistory: List<WeightHistoryEntry>
)

data class WeeklyTrainingSummary(
    val days: List<WeeklyTrainingDaySummary> = emptyList(),
    val workoutCount: Int = 0,
    val totalTonnage: Double = 0.0
)

data class WeeklyTrainingDaySummary(
    val date: LocalDate,
    val workoutCount: Int,
    val totalTonnage: Double
)

enum class ProgressProofType {
    STRENGTH,
    REPS,
    CONSISTENCY,
    BODY_WEIGHT
}

data class ProgressProof(
    val exerciseName: String,
    val previousLabel: String,
    val currentLabel: String,
    val deltaText: String,
    val percentageChange: Float,
    val proofType: ProgressProofType
)

data class WeeklySystemReport(
    val bestTrainingDay: String = "",
    val weakestPattern: String = "",
    val biggestProgress: String = "",
    val recoveryIssue: String = "",
    val nextWeekDecision: String = ""
)

data class SystemInsight(
    val improved: String = "",
    val weakPoint: String = "",
    val recommendation: String = ""
)
