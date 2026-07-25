package com.ihor.thesystem.domain.model

data class BetaMetrics(
    val onboardingCompleted: Boolean = false,
    val firstWorkoutLogged: Boolean = false,
    val plannedWorkoutsCompletedThisWeek: Int = 0,
    val plannedWorkoutsMissedThisWeek: Int = 0,
    val currentStreak: Int = 0,
    val daysAppOpenedOrRefreshed: Int = 0,
    val todayOrderDecisionDistribution: Map<TodayTrainingDecisionType, Int> =
        TodayTrainingDecisionType.entries.associateWith { 0 }
) {
    val hasSignal: Boolean
        get() = onboardingCompleted ||
            firstWorkoutLogged ||
            plannedWorkoutsCompletedThisWeek > 0 ||
            plannedWorkoutsMissedThisWeek > 0 ||
            currentStreak > 0 ||
            daysAppOpenedOrRefreshed > 0 ||
            todayOrderDecisionDistribution.any { it.value > 0 }
}

data class BetaMetricsEventState(
    val appOpenedEpochDays: Set<Long> = emptySet(),
    val todayOrderDecisionsByDay: Map<Long, TodayTrainingDecisionType> = emptyMap()
)
