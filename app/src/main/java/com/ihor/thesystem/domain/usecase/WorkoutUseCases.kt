package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.ViewingDateRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

data class WorkoutUseCases @Inject constructor(
    val saveExerciseSets: SaveExerciseSetsUseCase,
    val calculateRecommendation: CalculateRecommendedSetUseCase,
    val calculateCycleDay: CalculateCycleDayForDateUseCase,
    val getWorkoutDetails: GetWorkoutDetailsUseCase,
    val updateMatrixGoals: UpdateMatrixGoalsUseCase,
    val getPlayerFlow: GetPlayerFlowUseCase,
    val getStatisticsData: GetStatisticsDataUseCase,
    val finalizeSession: FinalizeSessionUseCase,
    private val scheduleRepo: ScheduleRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository
) {
    fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>> =
        scheduleRepo.getSchedulesForDays(days)

    val selectedDate: StateFlow<LocalDate?>
        get() = viewingDateRepo.selectedDate

    suspend fun getLastSetsForExercise(exerciseId: Int): List<ExerciseSet> =
        analyticsRepo.getLastSetsForExercise(exerciseId)

    fun getAllExercises(): Flow<List<ExerciseDetails>> =
        scheduleRepo.getAllExercises()

    suspend fun createExercise(name: String): Int =
        scheduleRepo.createExercise(name)

    suspend fun deleteExercise(exerciseId: Int) =
        scheduleRepo.deleteExercise(exerciseId)

    suspend fun saveWorkoutForDay(cycleDay: Int, workoutName: String, exerciseIds: List<Int>) =
        scheduleRepo.saveWorkoutForDay(cycleDay, workoutName, exerciseIds)

    suspend fun removeExerciseFromDay(cycleDay: Int, exerciseId: Int) =
        scheduleRepo.removeExerciseFromDay(cycleDay, exerciseId)
}
