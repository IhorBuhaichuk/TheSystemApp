package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseWorkoutData
import com.ihor.thesystem.domain.model.WorkoutDetailsData
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetWorkoutDetailsUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase
) {
    suspend operator fun invoke(day: Int): WorkoutDetailsData? {
        val schedule = scheduleRepo.getSchedulesForDays(listOf(day)).first().firstOrNull()
            ?: return null

        val exercises = schedule.exercises.map { exercise ->
            val recommendation = calculateRecommendation(exercise.id, exercise.name)
            ExerciseWorkoutData(
                exerciseId = exercise.id,
                name = exercise.name,
                nameUk = exercise.nameUk,
                recommendedWeight = recommendation.weight,
                recommendedReps = recommendation.reps,
                recommendedSets = recommendation.sets,
                gifUrl = exercise.gifUrl,
                externalId = exercise.externalId
            )
        }

        return WorkoutDetailsData(
            dayNumber = schedule.cycleDay,
            workoutName = schedule.workoutTemplateName,
            exercises = exercises
        )
    }
}

