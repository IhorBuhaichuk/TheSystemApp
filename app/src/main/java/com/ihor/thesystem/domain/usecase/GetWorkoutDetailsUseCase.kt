package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import com.ihor.thesystem.domain.model.ActiveSetInput
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetWorkoutDetailsUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase
) {
    suspend operator fun invoke(day: Int): ActiveDayUiModel? {
        val schedules = scheduleRepo.getSchedulesForDays(listOf(day)).first()
        val schedule = schedules.firstOrNull() ?: return null
        
        val exercisesWithRecs = schedule.exercises.map { ex ->
            val rec = calculateRecommendation(ex.id, ex.name)
            ExerciseWorkoutUiModel(
                exerciseId = ex.id,
                name = ex.name,
                recommendedWeight = rec.weight,
                recommendedReps = rec.reps,
                recommendedSets = rec.sets,
                recommendation = "${rec.sets}x${rec.reps} @ ${rec.weight}kg",
                gifUrl = ex.gifUrl,
                sets = (1..(rec.sets ?: 1)).map { ActiveSetInput() }.toImmutableList()
            )
        }.toImmutableList()

        return ActiveDayUiModel(
            dayNumber = schedule.cycleDay,
            dailyTasks = persistentListOf(),
            workoutName = schedule.workoutTemplateName,
            exercises = exercisesWithRecs,
            matrixEntries = persistentListOf()
        )
    }
}
