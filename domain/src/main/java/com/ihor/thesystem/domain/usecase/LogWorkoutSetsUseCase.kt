package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class LogWorkoutSetsUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val transactionProvider: TransactionProvider
) {
    suspend operator fun invoke(
        sessionId: Long?,
        exerciseId: Int,
        sets: List<ActiveSetInput>,
        timestamp: Long,
        userFeedback: String? = null
    ) {
        val validSets = sets.filter {
            it.weight.toDoubleOrNull()?.let { weight -> weight > 0 } == true &&
                it.reps.toIntOrNull()?.let { reps -> reps > 0 } == true
        }
        if (validSets.isEmpty()) return

        val player = playerRepo.getPlayer().firstOrNull()
        val currentCycleDay = player?.currentCycleDay ?: 0

        val totalTonnage = validSets.filter { it.isCompleted }.sumOf {
            (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0)
        }

        transactionProvider.runInTransaction {
            if (sessionId != null) {
                replaceExistingLog(
                    exerciseId = exerciseId,
                    validSets = validSets,
                    existingSessionId = sessionId,
                    userFeedback = userFeedback
                )
            } else {
                insertNewLog(
                    exerciseId = exerciseId,
                    validSets = validSets,
                    timestamp = timestamp,
                    totalTonnage = totalTonnage,
                    currentCycleDay = currentCycleDay,
                    userFeedback = userFeedback
                )
            }

            validSets.filter { it.isCompleted }
                .mapNotNull { it.weight.toFloatOrNull() }
                .maxOrNull()
                ?.let { maxWeight -> matrixRepo.updateCurrentWeight(exerciseId, maxWeight) }
        }
    }

    private suspend fun replaceExistingLog(
        exerciseId: Int,
        validSets: List<ActiveSetInput>,
        existingSessionId: Long,
        userFeedback: String?
    ) {
        analyticsRepo.replaceExerciseSets(
            sessionId = existingSessionId,
            exerciseId = exerciseId,
            sets = validSets.map { input ->
                input.toExerciseSet(
                    exerciseId = exerciseId,
                    sessionId = existingSessionId,
                    userFeedback = userFeedback
                )
            }
        )
    }

    private suspend fun insertNewLog(
        exerciseId: Int,
        validSets: List<ActiveSetInput>,
        timestamp: Long,
        totalTonnage: Double,
        currentCycleDay: Int,
        userFeedback: String?
    ) {
        val sessionLog = WorkoutSession(
            questId = 0,
            timestamp = timestamp,
            totalTonnage = totalTonnage,
            cycleDay = currentCycleDay,
            durationMinutes = 0
        )

        analyticsRepo.saveFullSessionLog(
            sessionLog,
            validSets.map { input ->
                input.toExerciseSet(
                    exerciseId = exerciseId,
                    sessionId = 0,
                    userFeedback = userFeedback
                )
            }
        )
    }

    private fun ActiveSetInput.toExerciseSet(
        exerciseId: Int,
        sessionId: Long,
        userFeedback: String?
    ): ExerciseSet =
        ExerciseSet(
            sessionId = sessionId,
            exerciseId = exerciseId,
            weight = weight.toDoubleOrNull() ?: 0.0,
            reps = reps.toIntOrNull() ?: 0,
            isCompleted = isCompleted,
            userFeedback = userFeedback
        )
}
