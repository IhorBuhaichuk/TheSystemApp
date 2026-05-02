package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.domain.model.MatrixEntryData
import com.ihor.thesystem.domain.model.StatisticsData
import kotlinx.collections.immutable.toImmutableList

fun StatisticsData.toStatisticsUiData() = StatisticsUiData(
    playerName = playerName,
    playerClass = playerClass,
    level = level,
    xpTotal = xpTotal,
    xpMax = xpMax,
    currentMonth = currentMonth,
    totalMonths = totalMonths,
    currentWeek = currentWeek,
    currentCycleDay = currentCycleDay,
    isPenaltyActive = isPenaltyActive,
    globalRank = globalRank,
    currentWeight = currentWeight,
    currentHeight = currentHeight,
    age = age,
    matrixEntries = matrixEntries.map { it.toMatrixEntryUiModel() }.toImmutableList(),
    weightHistory = weightHistory.toImmutableList(),
    characterAttributes = characterAttributes,
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    xpThisWeek = xpThisWeek,
    weeklySummary = WeeklyTrainingSummaryUiModel(
        days = weeklySummary.days.map { day ->
            WeeklyTrainingDayUiModel(
                label = shortDayLabel(day.date.dayOfWeek.value),
                workoutCount = day.workoutCount,
                totalTonnage = day.totalTonnage
            )
        }.toImmutableList(),
        workoutCount = weeklySummary.workoutCount,
        totalTonnage = weeklySummary.totalTonnage
    ),
    systemInsight = SystemInsightUiModel(
        improved = systemInsight.improved,
        weakPoint = systemInsight.weakPoint,
        recommendation = systemInsight.recommendation
    ),
    avatarUri = avatarUri
)

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

private fun shortDayLabel(dayOfWeekValue: Int): String = when (dayOfWeekValue) {
    1 -> "Пн"
    2 -> "Вт"
    3 -> "Ср"
    4 -> "Чт"
    5 -> "Пт"
    6 -> "Сб"
    else -> "Нд"
}
