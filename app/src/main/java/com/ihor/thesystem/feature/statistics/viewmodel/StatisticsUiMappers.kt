package com.ihor.thesystem.feature.statistics.viewmodel

import com.ihor.thesystem.domain.model.BetaMetrics
import com.ihor.thesystem.domain.model.StatisticsData
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.presentation.common.model.toMatrixEntryUiModel
import kotlinx.collections.immutable.toImmutableList

fun StatisticsData.toStatisticsUiData(
    betaMetrics: BetaMetrics = BetaMetrics()
) = StatisticsUiData(
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
    progressProofs = progressProofs.map { proof ->
        ProgressProofUiModel(
            exerciseName = proof.exerciseName,
            previousLabel = proof.previousLabel,
            currentLabel = proof.currentLabel,
            deltaText = proof.deltaText,
            percentageChange = proof.percentageChange,
            proofType = proof.proofType
        )
    }.toImmutableList(),
    weeklySystemReport = WeeklySystemReportUiModel(
        bestTrainingDay = weeklySystemReport.bestTrainingDay,
        weakestPattern = weeklySystemReport.weakestPattern,
        biggestProgress = weeklySystemReport.biggestProgress,
        recoveryIssue = weeklySystemReport.recoveryIssue,
        nextWeekDecision = weeklySystemReport.nextWeekDecision
    ),
    nutritionFloorStatus = nutritionFloorStatus,
    systemInsight = SystemInsightUiModel(
        improved = systemInsight.improved,
        weakPoint = systemInsight.weakPoint,
        recommendation = systemInsight.recommendation
    ),
    betaMetrics = betaMetrics.toUiModel(),
    avatarUri = avatarUri
)

private fun BetaMetrics.toUiModel(): BetaMetricsUiModel =
    BetaMetricsUiModel(
        onboardingCompleted = onboardingCompleted,
        firstWorkoutLogged = firstWorkoutLogged,
        plannedCompletedThisWeek = plannedWorkoutsCompletedThisWeek,
        plannedMissedThisWeek = plannedWorkoutsMissedThisWeek,
        currentStreak = currentStreak,
        daysAppOpenedOrRefreshed = daysAppOpenedOrRefreshed,
        decisionDistribution = todayOrderDecisionDistribution
            .filterValues { it > 0 }
            .entries
            .sortedWith(compareByDescending<Map.Entry<TodayTrainingDecisionType, Int>> { it.value }.thenBy { it.key.name })
            .map { (type, count) ->
                BetaDecisionDistributionUiModel(
                    label = type.betaLabel(),
                    count = count
                )
            }
            .toImmutableList()
    )

private fun TodayTrainingDecisionType.betaLabel(): String =
    when (this) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> "Посилене тренування"
        TodayTrainingDecisionType.STANDARD_TRAINING -> "Звичайне тренування"
        TodayTrainingDecisionType.REDUCED_LOAD -> "Легше тренування"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Відновлення"
        TodayTrainingDecisionType.NO_EXCUSE -> "Коротке тренування"
        TodayTrainingDecisionType.DELOAD -> "Легше тренування"
        TodayTrainingDecisionType.REST -> "Відпочинок"
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
