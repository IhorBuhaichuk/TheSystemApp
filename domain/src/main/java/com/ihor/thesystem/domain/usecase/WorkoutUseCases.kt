package com.ihor.thesystem.domain.usecase

import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

data class WorkoutUseCases @Inject constructor(
    val saveExerciseSets: SaveExerciseSetsUseCase,
    val calculateRecommendation: CalculateRecommendedSetUseCase,
    val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    val getActiveWorkoutQuest: GetActiveWorkoutQuestUseCase,
    val getWorkoutDetails: GetWorkoutDetailsUseCase,
    val updateMatrixGoals: UpdateMatrixGoalsUseCase,
    val getPlayerFlow: GetPlayerFlowUseCase,
    val getStatisticsData: GetStatisticsDataUseCase,
    val finalizeSession: FinalizeSessionUseCase,
    val syncCycleAnchor: SyncCycleAnchorUseCase,
    val getSchedulesForDays: GetSchedulesForDaysUseCase,
    val getAllExercises: GetAllExercisesUseCase,
    val createExercise: CreateExerciseUseCase,
    val deleteExercise: DeleteExerciseUseCase,
    val updateExerciseTrackingMode: UpdateExerciseTrackingModeUseCase,
    val saveWorkoutForDay: SaveWorkoutForDayUseCase,
    val removeExerciseFromDay: RemoveExerciseFromDayUseCase,
    val getLastSetsForExercise: GetLastSetsForExerciseUseCase,
    val decideTodayWorkout: DecideTodayWorkoutUseCase,
    private val getSelectedViewingDate: GetSelectedViewingDateUseCase,
    val selectToday: SelectTodayUseCase,
    val selectDate: SelectViewingDateUseCase
) {
    val selectedDate: StateFlow<LocalDate?>
        get() = getSelectedViewingDate()
}
