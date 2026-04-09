package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.OneRepMaxCalculator
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.feature.statistics.model.AnnualMatrixProvider
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetProgressionMatrixUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    operator fun invoke(): Flow<List<ProgressionMatrixEntry>> = repo.getAllEntries()
}

class UpdateExerciseWeightUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, newWeight: Float) {
        if (newWeight > 0f) repo.updateCurrentWeight(exerciseId, newWeight)
    }
}

class SaveExerciseSetsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository,
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase
) {
    suspend operator fun invoke(exerciseId: Int, sets: List<WorkoutSetInput>, timestamp: Long, userFeedback: String?) {
        // 1. Збереження логу підходів
        matrixRepo.saveExerciseSetsWithDate(exerciseId, sets, timestamp, userFeedback)

        // 2. Алгоритм автоматичного підвищення рангу (Завдання 3)
        val validSets = sets.filter { it.weight.isNotEmpty() && it.reps.isNotEmpty() }
        if (validSets.isEmpty()) return

        val maxWeight = validSets.mapNotNull { it.weight.toDoubleOrNull() }.maxOrNull() ?: return
        val maxReps = validSets.filter { it.weight.toDoubleOrNull() == maxWeight }
            .mapNotNull { it.reps.toIntOrNull() }.maxOrNull() ?: 1

        // - Розрахуй 1RM введеного результату
        val current1RM = OneRepMaxCalculator.calculate(maxWeight, maxReps)

        // - Отримай поточну вправу та вагу гравця
        val entry = matrixRepo.getEntrySync(exerciseId) ?: return
        val playerWeight = playerRepo.getLatestWeight().firstOrNull()?.toDouble() ?: 80.0

        // - Визнач newRank за допомогою AnnualMatrixProvider
        val newRank = AnnualMatrixProvider.getExerciseRank(entry.exerciseName, current1RM, playerWeight)

        // - Перевір математичну вагу (weight: E=1..S=6)
        if (newRank.weight > entry.currentRank.weight) {
            // - Онови currentRank у базі даних
            matrixRepo.updateRank(exerciseId, newRank)
            
            // - Онови середній арифметичний ранг гравця
            recalculateGlobalRank()
        }
    }
}
