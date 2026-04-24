package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.ViewingDateRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import javax.inject.Inject

data class WorkoutUseCases @Inject constructor(
    val saveExerciseSets: SaveExerciseSetsUseCase,
    val calculateRecommendation: CalculateRecommendedSetUseCase,
    val calculateCycleDay: CalculateCycleDayForDateUseCase,
    val getWorkoutDetails: GetWorkoutDetailsUseCase,
    val updateMatrixGoals: UpdateMatrixGoalsUseCase,
    val getPlayerFlow: GetPlayerFlowUseCase,
    val getStatisticsData: GetStatisticsDataUseCase,
    val scheduleRepo: ScheduleRepository,
    val viewingDateRepo: ViewingDateRepository,
    val analyticsRepo: WorkoutAnalyticsRepository
)
