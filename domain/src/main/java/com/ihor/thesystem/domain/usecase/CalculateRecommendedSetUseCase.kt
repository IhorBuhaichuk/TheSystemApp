package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import javax.inject.Inject
import kotlin.math.abs

data class SetRecommendation(
    val weight: Double,
    val reps: Int,
    val sets: Int = TARGET_SETS,
    val isProgression: Boolean,
    val exerciseId: Int = 0
)

class CalculateRecommendedSetUseCase @Inject constructor(
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val matrixRepo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, exerciseName: String): SetRecommendation {
        val sets = analyticsRepo.getLastSetsForExercise(exerciseId)
        return fromSets(exerciseId, exerciseName, sets)
    }

    suspend fun fromSets(
        exerciseId: Int,
        exerciseName: String,
        sets: List<ExerciseSet>
    ): SetRecommendation {
        val entry = matrixRepo.getEntrySync(exerciseId)
        return fromSets(exerciseId, exerciseName, sets, entry)
    }

    fun fromSets(
        exerciseId: Int,
        exerciseName: String,
        sets: List<ExerciseSet>,
        entry: ProgressionMatrixEntry?
    ): SetRecommendation {
        val startWeight = entry?.startWeight?.toDouble() ?: 0.0
        val progressionStep = entry?.weeklyStep
            ?.takeIf { it > 0f }
            ?.toDouble()
            ?: DEFAULT_PROGRESSION_STEP

        val completedSets = sets.filter { it.isCompleted && it.weight > 0.0 && it.reps > 0 }

        if (completedSets.isEmpty()) {
            return SetRecommendation(
                weight = startWeight,
                reps = TARGET_REPS,
                isProgression = false,
                exerciseId = exerciseId
            )
        }

        val workingWeight = completedSets.maxOf { it.weight }
        val workingSets = completedSets.filter { it.weight.isSameLoadAs(workingWeight) }
        val wasSuccessful = workingSets.size >= TARGET_SETS && workingSets.all { it.reps >= TARGET_REPS }

        return if (wasSuccessful) {
            val targetCap = entry?.targetWeight
                ?.takeIf { it > 0f }
                ?.toDouble()
            val nextWeight = (workingWeight + progressionStep)
                .let { proposed -> targetCap?.let { minOf(proposed, it) } ?: proposed }

            SetRecommendation(
                weight = nextWeight,
                reps = RESET_REPS,
                sets = TARGET_SETS,
                isProgression = true,
                exerciseId = exerciseId
            )
        } else {
            val nextReps = (workingSets.maxOfOrNull { it.reps } ?: RESET_REPS)
                .plus(1)
                .coerceIn(RESET_REPS, TARGET_REPS)

            SetRecommendation(
                weight = workingWeight,
                reps = nextReps,
                sets = TARGET_SETS,
                isProgression = false,
                exerciseId = exerciseId
            )
        }
    }

    private fun Double.isSameLoadAs(other: Double): Boolean =
        abs(this - other) < LOAD_EPSILON
}

private const val TARGET_SETS = 3
private const val TARGET_REPS = 12
private const val RESET_REPS = 8
private const val DEFAULT_PROGRESSION_STEP = 2.5
private const val LOAD_EPSILON = 0.001
