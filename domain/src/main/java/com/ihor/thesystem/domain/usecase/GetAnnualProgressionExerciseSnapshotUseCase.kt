package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.OneRepMaxCalculator
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseSnapshot
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAnnualProgressionExerciseSnapshotUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val progressionMatrixRepository: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int): AnnualProgressionExerciseSnapshot? {
        val exercise = workoutRepository.getAllExercisesSync()
            .firstOrNull { it.id == exerciseId }
            ?: return null
        val matrixEntry = progressionMatrixRepository.getEntrySync(exerciseId)
        val reference = progressionMatrixRepository.getReferenceForExercise(exerciseId)
        val trackingMode = ExerciseTrackingModeResolver.resolve(exercise, reference)
        val logs = analyticsRepository.getAllLogs().first()
        val latestExerciseLog = logs.firstOrNull { log ->
            log.sets.any { it.exerciseId == exerciseId }
        }
        val latestCompletedSets = latestExerciseLog
            ?.sets
            ?.filter { it.exerciseId == exerciseId && it.isCompleted && it.weight > 0.0 && it.reps > 0 }
            .orEmpty()
        val weightedCompletedSets = if (trackingMode.usesWeightInput) {
            latestCompletedSets.filter { it.weight > TECHNICAL_BODYWEIGHT_LOAD }
        } else {
            emptyList()
        }
        val topSet = weightedCompletedSets.maxWithOrNull(
            compareBy<ExerciseSet> { it.weight }.thenBy { it.reps }
        )
        val estimatedOneRepMax = weightedCompletedSets
            .maxOfOrNull { OneRepMaxCalculator.calculate(it.weight, it.reps) }

        val currentWorkingWeight = if (trackingMode.usesWeightInput) {
            topSet?.weight
                ?: matrixEntry?.currentWeight?.toDouble()?.takeIf { it > TECHNICAL_BODYWEIGHT_LOAD }
        } else {
            null
        }
        val existingTarget = matrixEntry?.targetWeight
            ?.toDouble()
            ?.takeIf { target -> currentWorkingWeight == null || target > currentWorkingWeight }

        return AnnualProgressionExerciseSnapshot(
            exercise = exercise,
            currentWorkingWeight = currentWorkingWeight,
            reps = topSet?.reps,
            lastTrainingTimestamp = latestExerciseLog?.session?.timestamp,
            estimatedOneRepMax = estimatedOneRepMax,
            defaultTargetWeight = existingTarget,
            inventoryStep = reference?.progressionStep?.takeIf { it > 0.0 } ?: DEFAULT_INVENTORY_STEP
        )
    }
}

private const val DEFAULT_INVENTORY_STEP = 2.5
private const val TECHNICAL_BODYWEIGHT_LOAD = 1.0
