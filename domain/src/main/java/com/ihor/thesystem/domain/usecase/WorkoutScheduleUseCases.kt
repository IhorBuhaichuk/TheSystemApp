package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.ViewingDateRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class GetSchedulesForDaysUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(days: List<Int>): Flow<List<ScheduleDay>> =
        scheduleRepository.getSchedulesForDays(days)
}

class GetAllExercisesUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<ExerciseDetails>> =
        scheduleRepository.getAllExercises()
}

class CreateExerciseUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(name: String): Int =
        scheduleRepository.createExercise(name)
}

class DeleteExerciseUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(exerciseId: Int) {
        scheduleRepository.deleteExercise(exerciseId)
    }
}

class UpdateExerciseTrackingModeUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(exerciseId: Int, trackingMode: String?) {
        scheduleRepository.updateExerciseTrackingMode(exerciseId, trackingMode)
    }
}

class SaveWorkoutForDayUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(cycleDay: Int, workoutName: String, exerciseIds: List<Int>) {
        scheduleRepository.saveWorkoutForDay(cycleDay, workoutName, exerciseIds)
    }
}

class RemoveExerciseFromDayUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(cycleDay: Int, exerciseId: Int) {
        scheduleRepository.removeExerciseFromDay(cycleDay, exerciseId)
    }
}

class GetLastSetsForExerciseUseCase @Inject constructor(
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository
) {
    suspend operator fun invoke(exerciseId: Int): List<ExerciseSet> =
        workoutAnalyticsRepository.getLastSetsForExercise(exerciseId)
}

class GetSelectedViewingDateUseCase @Inject constructor(
    private val viewingDateRepository: ViewingDateRepository
) {
    operator fun invoke(): StateFlow<LocalDate?> =
        viewingDateRepository.selectedDate
}

class SelectTodayUseCase @Inject constructor(
    private val viewingDateRepository: ViewingDateRepository
) {
    operator fun invoke() {
        viewingDateRepository.selectToday()
    }
}
